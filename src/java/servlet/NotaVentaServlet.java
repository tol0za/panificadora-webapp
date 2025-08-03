package servlet;

import dao.*;
import dto.DistribucionResumen;
import dto.InventarioDTO;
import modelo.DetalleNotaVenta;
import modelo.NotaVenta;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del módulo Notas de Venta — CRUD + inventarios.
 */

public class NotaVentaServlet extends HttpServlet {

    private NotaVentaDAO notaDAO;
    private DetalleNotaDAO detalleDAO;
    private InventarioRepartidorDAO invRepDAO;
    private InventarioEmpaquetadoDAO invGlobalDAO;
    private DistribucionDAO distribucionDAO;
    private RepartidorDAO repartidorDAO;
    private TiendaDAO tiendaDAO;

    @Override
    public void init() throws ServletException {
        try {
            notaDAO        = new NotaVentaDAO();
            detalleDAO     = new DetalleNotaDAO();
            invRepDAO      = new InventarioRepartidorDAO();
            invGlobalDAO   = new InventarioEmpaquetadoDAO();
            distribucionDAO= new DistribucionDAO();
            repartidorDAO  = new RepartidorDAO();
            tiendaDAO      = new TiendaDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al inicializar DAOs", e);
        }
    }

    /* ############ helpers iframe ############ */
  private boolean fromIframe(HttpServletRequest req){
    if ("1".equals(req.getParameter("inFrame"))) return true;   // 👍 nuevo
    String ref = req.getHeader("referer");
    return ref != null && ref.contains("/jsp/home/inicio.jsp");
}
    private void forwardVista(HttpServletRequest req,HttpServletResponse res,String vista) throws ServletException,IOException{
        if(!fromIframe(req))
            res.sendRedirect(req.getContextPath()+"/jsp/home/inicio.jsp?vista="+vista);
        else
            req.getRequestDispatcher("/"+vista).forward(req,res);
    }

    /* ############ entry ############ */
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s) throws ServletException,IOException{process(r,s);}    
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s) throws ServletException,IOException{process(r,s);}   
    private void process(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException{
        String acc = req.getParameter("accion");
        if(acc==null) acc="repartidores";
        try{
            switch(acc){
                case "repartidores"    -> repartidoresHoy(req,res);
                case "vistaRepartidor" -> vistaRepartidor(req,res);
                case "guardarNota"     -> guardarNota(req,res);
                case "editarNota"      -> editarForm(req,res);
                case "actualizarNota"  -> actualizarNota(req,res);
                case "eliminarNota"    -> eliminarNota(req,res);
                case "cerrarRuta"      -> cerrarRuta(req,res);
                default                 -> repartidoresHoy(req,res);
            }
        }catch(SQLException e){ throw new ServletException(e);}    }

    /* ===== 1. Repartidores hoy ===== */
    private void repartidoresHoy(HttpServletRequest req,HttpServletResponse res) throws SQLException,ServletException,IOException{
        LocalDate hoy = LocalDate.now();
        List<DistribucionResumen> lista = distribucionDAO.repartidoresConSalida(hoy);
        req.setAttribute("listaRepartidores",lista);
        req.setAttribute("hoy",hoy);
        forwardVista(req,res,"jsp/notas/RepartidoresConSalidaHoy.jsp");
    }

    /* ===== 2. Vista del repartidor ===== */
/* =========================================================
 *  Vista del día para un repartidor
 * ========================================================= */
private void vistaRepartidor(HttpServletRequest req,
                             HttpServletResponse res)
        throws SQLException, ServletException, IOException {

    int idRep = Integer.parseInt(req.getParameter("id"));
    LocalDate hoy = LocalDate.now();

    /* ---------- Inventario que lleva aún el repartidor ---------- */
    List<InventarioDTO> inventario = distribucionDAO.inventarioPendiente(idRep, hoy);

    /* ---------- Encabezados de notas del día ---------- */
    List<NotaVenta> notas = notaDAO.listarPorRepartidorYFecha(idRep, hoy);

    /* ---------- Enriquecer cada nota con datos de apoyo ---------- */
    for (NotaVenta n : notas) {
        /* nombre de tienda */
        String nomTienda = tiendaDAO.buscarPorId(n.getIdTienda()).getNombre();
        n.setNombreTienda(nomTienda);

        /* total de la nota                             (NEW) */
        double tot = detalleDAO.obtenerTotalPorNota(n.getIdNotaVenta());
        n.setTotal(tot);
    }

    /* ---------- Total global del día (ya existía) ---------- */
    double totalDia = notaDAO.getTotalDia(idRep, hoy);

    /* ---------- Atributos para el JSP ---------- */
    req.setAttribute("inventario",     inventario);
    req.setAttribute("inventarioJson", toJson(inventario));
    req.setAttribute("listaNotas",     notas);
    req.setAttribute("totalDia",       String.format("%.2f", totalDia));
    req.setAttribute("repartidor",     repartidorDAO.obtener(idRep));
    req.setAttribute("tiendas",        tiendaDAO.listarTodas());
    req.setAttribute("hoy",            hoy);

    forwardVista(req, res, "jsp/notas/NotaDiaRepartidor.jsp");
}


   /* ===== 3. Guardar nota ===== */
private void guardarNota(HttpServletRequest req,
                         HttpServletResponse res)
        throws SQLException, ServletException, IOException {

    int folio     = Integer.parseInt(req.getParameter("folio"));
    int idRep     = Integer.parseInt(req.getParameter("id_repartidor"));
    int idTienda  = Integer.parseInt(req.getParameter("id_tienda"));

    // ­­­── folio duplicado
    if (notaDAO.folioExiste(folio)) {
        req.setAttribute("mensaje", "El folio " + folio + " ya existe");
        req.setAttribute("tipoMensaje", "error");
        vistaRepartidor(req, res);
        return;
    }

    /* ---------- encabezado ---------- */
    NotaVenta n = new NotaVenta();
    n.setFolio(folio);
    n.setIdRepartidor(idRep);
    n.setIdTienda(idTienda);
    n.setFechaNota(LocalDateTime.now());
    n.setTotal(0);

    int idNota = notaDAO.insertar(n);

    /* ---------- detalle ---------- */
    List<DetalleNotaVenta> lineas =
        parseDetalleJSON(req.getParameter("lineas"), idNota);

    for (DetalleNotaVenta d : lineas) {
        detalleDAO.insertar(d);

        int piezas = d.getCantidadVendida() + d.getMerma();
        invRepDAO.descontar(idRep, d.getIdEmpaque(), piezas);
        invGlobalDAO.registrarMovimientoSalida(
                d.getIdEmpaque(), piezas, d.getIdDistribucion(), idRep);
    }

    notaDAO.actualizarTotal(idNota);

    /* ---------- mensaje flash en sesión ---------- */
    req.getSession().setAttribute("flashMsg", "Nota guardada correctamente");

    /* ---------- redirige (GET) con el id de repartidor ---------- */
    res.sendRedirect(req.getContextPath()
          + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
}


    /* ===== 4. Form editar ===== */
    private void editarForm(HttpServletRequest req,HttpServletResponse res) throws SQLException,ServletException,IOException{
        int idNota = Integer.parseInt(req.getParameter("id"));
        NotaVenta n = notaDAO.obtener(idNota);
        List<DetalleNotaVenta> det = detalleDAO.listarPorNota(idNota);
        req.setAttribute("nota",n);
        req.setAttribute("detalle",det);
        req.setAttribute("inventario",distribucionDAO.inventarioPendiente(n.getIdRepartidor(),LocalDate.now()));
        req.setAttribute("tiendas",tiendaDAO.listarTodas());
        forwardVista(req,res,"jsp/notas/NotaFormEditar.jsp");
    }

    /* ===== 5. Actualizar nota ===== */
    private void actualizarNota(HttpServletRequest req,HttpServletResponse res) throws SQLException,ServletException,IOException{
        int idNota=Integer.parseInt(req.getParameter("id_nota"));
        int folio=Integer.parseInt(req.getParameter("folio"));
        int idTienda=Integer.parseInt(req.getParameter("id_tienda"));
        NotaVenta n=notaDAO.obtener(idNota);
        if(n==null){repartidoresHoy(req,res);return;}
        if(n.getFolio()!=folio && notaDAO.folioExiste(folio)){
            req.setAttribute("mensaje","Folio duplicado");req.setAttribute("tipoMensaje","error");
            editarForm(req,res);return;
        }
        // revertir inventario anterior
        for(DetalleNotaVenta d:detalleDAO.listarPorNota(idNota)){
            int piezas=d.getCantidadVendida()+d.getMerma();
            invRepDAO.devolver(n.getIdRepartidor(),d.getIdEmpaque(),piezas);
            invGlobalDAO.regresarStockGeneral(d.getIdEmpaque(),piezas);
        }
        detalleDAO.eliminarPorNota(idNota);
        // insertar nuevos detalles
        for(DetalleNotaVenta d:parseDetalleJSON(req.getParameter("lineas"),idNota)){
            detalleDAO.insertar(d);
            int piezas=d.getCantidadVendida()+d.getMerma();
            invRepDAO.descontar(n.getIdRepartidor(),d.getIdEmpaque(),piezas);
            invGlobalDAO.registrarMovimientoSalida(d.getIdEmpaque(),piezas,d.getIdDistribucion(),n.getIdRepartidor());
        }
        n.setFolio(folio);n.setIdTienda(idTienda);
        notaDAO.actualizar(n);
        notaDAO.actualizarTotal(idNota);
        req.setAttribute("mensaje","Nota actualizada");req.setAttribute("tipoMensaje","success");
        vistaRepartidor(req,res);
    }

     /* ========== 6) Eliminar nota ========== */
    private void eliminarNota(HttpServletRequest req,HttpServletResponse res) throws SQLException,ServletException,IOException{
        int idNota=Integer.parseInt(req.getParameter("id"));
        NotaVenta n=notaDAO.obtener(idNota);
        if(n==null){repartidoresHoy(req,res);return;}
        List<DetalleNotaVenta> det=detalleDAO.listarPorNota(idNota);
        for(DetalleNotaVenta d:det){
            int piezas=d.getCantidadVendida()+d.getMerma();
            invRepDAO.devolver(n.getIdRepartidor(),d.getIdEmpaque(),piezas);
            invGlobalDAO.regresarStockGeneral(d.getIdEmpaque(),piezas);
        }
        detalleDAO.eliminarPorNota(idNota);notaDAO.eliminar(idNota);
        req.setAttribute("mensaje","Nota eliminada");req.setAttribute("tipoMensaje","success");
        vistaRepartidor(req,res);
    }

    /* ========== 7) Cerrar ruta ========== */
    private void cerrarRuta(HttpServletRequest req,HttpServletResponse res) throws SQLException,ServletException,IOException{
        int idRep=Integer.parseInt(req.getParameter("id_repartidor"));
        LocalDate hoy=LocalDate.now();
        List<InventarioDTO> pendiente=distribucionDAO.inventarioPendiente(idRep,hoy);
        for(InventarioDTO inv:pendiente){
            if(inv.getRestante()>0){
                invGlobalDAO.regresarStockGeneral(inv.getIdEmpaque(),inv.getRestante());
                invRepDAO.descontar(idRep,inv.getIdEmpaque(),-inv.getRestante());
            }
        }
        req.setAttribute("mensaje","Ruta cerrada y sobrante devuelto");req.setAttribute("tipoMensaje","success");
        repartidoresHoy(req,res);
    }

    /* ========== helpers JSON & parsing ========== */
    private String toJson(List<InventarioDTO> lista){
        StringBuilder sb = new StringBuilder("{");
        for(int i = 0; i < lista.size(); i++){
            InventarioDTO e = lista.get(i);
            sb.append('"').append(e.getIdEmpaque()).append("\":{")
              .append("\"nombre\":\"").append(e.getNombre()).append("\",")
              .append("\"precio\":").append(e.getPrecio()).append(',')
              .append("\"restante\":").append(e.getRestante()).append('}');
            if(i < lista.size() - 1) sb.append(',');
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Parsea JSON plano [{idEmpaque,idDistribucion,vendidas,merma,precio}]
     * → convierte a lista de DetalleNotaVenta.
     * Se deja sin implementación para compilar si no usas Gson todavía.
     */
  
    private List<DetalleNotaVenta> parseDetalleJSON(String json, int idNota) {
    return new ArrayList<>();
}
        
}
