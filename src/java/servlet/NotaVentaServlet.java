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
import com.google.gson.*;
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
    /* ############ entry point ############ */
@Override protected void doGet(HttpServletRequest r,HttpServletResponse s)
        throws ServletException,IOException{ process(r,s); }

@Override protected void doPost(HttpServletRequest r,HttpServletResponse s)
        throws ServletException,IOException{ process(r,s); }

private void process(HttpServletRequest req,HttpServletResponse res)
        throws ServletException,IOException {

    /* ---------- END-POINT AJAX: valida folio ---------- */
    if ("folioCheck".equals(req.getParameter("accion"))) {
        boolean duplicado = true;                       // =true si algo falla
        try {
            int f = Integer.parseInt(req.getParameter("folio"));
            duplicado = notaDAO.folioExiste(f);         // puede lanzar SQLException
        } catch (SQLException | NumberFormatException ignored) { }
        res.setContentType("text/plain");
        res.getWriter().print(duplicado ? "1" : "0");
        return;                                         // ← corta el flujo
    }
    /* -------------------------------------------------- */

    String acc = req.getParameter("accion");
    if (acc == null) acc = "repartidores";

    try {
        switch (acc) {
            case "repartidores"    -> repartidoresHoy(req,res);
            case "vistaRepartidor" -> vistaRepartidor(req,res);
            case "guardarNota"     -> guardarNota(req,res);
            case "editarNota"      -> editarForm(req,res);
            case "actualizarNota"  -> actualizarNota(req,res);
            case "eliminarNota"    -> eliminarNota(req,res);
            case "cerrarRuta"      -> cerrarRuta(req,res);
            default                -> repartidoresHoy(req,res);
        }
    } catch (SQLException e) {
        throw new ServletException(e);
    }
}

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

    List<InventarioDTO> inventario =
            distribucionDAO.inventarioPendiente(idRep, hoy);

    List<NotaVenta> notas =
            notaDAO.listarPorRepartidorYFecha(idRep, hoy);

    double totalDia = 0;
    for (NotaVenta n : notas){
        // nombre de la tienda
        n.setNombreTienda(
            tiendaDAO.buscarPorId(n.getIdTienda()).getNombre());

        // total de la nota
        double tot = detalleDAO.obtenerTotalPorNota(n.getIdNotaVenta());
        n.setTotal(tot);
        totalDia += tot;
    }

    req.setAttribute("inventario",     inventario);
    req.setAttribute("inventarioJson", toJson(inventario));
    req.setAttribute("listaNotas",     notas);
    req.setAttribute("totalDia",       totalDia);          // ⇦ double puro
    req.setAttribute("repartidor",     repartidorDAO.obtener(idRep));
    req.setAttribute("tiendas",        tiendaDAO.listarTodas());
    req.setAttribute("hoy",            hoy);

    forwardVista(req, res, "jsp/notas/NotaDiaRepartidor.jsp");
}

  /* ===== 3) Guardar nota ===== */
private void guardarNota(HttpServletRequest req,
                         HttpServletResponse res)
        throws SQLException, ServletException, IOException {

    int folio    = Integer.parseInt(req.getParameter("folio"));
    int idRep    = Integer.parseInt(req.getParameter("id_repartidor"));
    int idTienda = Integer.parseInt(req.getParameter("id_tienda"));

    /* ---------- VALIDAR FOLIO DUPLICADO ---------- */
    if (notaDAO.folioExiste(folio)) {
        req.getSession().setAttribute("flashMsg",
            "El folio&nbsp;<strong>"+folio+"</strong>&nbsp;ya existe");
        res.sendRedirect(req.getContextPath()
           + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
        return;
    }

    /* ---------- INSERT ENCABEZADO ---------- */
    NotaVenta n = new NotaVenta();
    n.setFolio(folio);
    n.setIdRepartidor(idRep);
    n.setIdTienda(idTienda);
    n.setFechaNota(LocalDateTime.now());
    n.setTotal(0);

    int idNota = notaDAO.insertar(n);           // folio único garantizado

    /* ---------- PARSE + INSERT DETALLE ---------- */
    List<DetalleNotaVenta> det =
        parseDetalleJSON(req.getParameter("lineas"), idNota);

    for (DetalleNotaVenta d : det) {
        detalleDAO.insertar(d);
        int piezas = d.getCantidadVendida() + d.getMerma();
        invRepDAO.descontar(idRep, d.getIdEmpaque(), piezas);
        invGlobalDAO.registrarMovimientoSalida(
            d.getIdEmpaque(), piezas, d.getIdDistribucion(), idRep);
    }

    notaDAO.actualizarTotal(idNota);            // suma total_linea

    /* ---------- REDIRECT CON FLASH ---------- */
    req.getSession().setAttribute("flashMsg", "Nota guardada");
    res.sendRedirect(req.getContextPath()
        + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
}



    /* ===== 4. Form editar ===== */
 private void editarForm(HttpServletRequest req,
                        HttpServletResponse res)
        throws SQLException, ServletException, IOException {

    int idNota = Integer.parseInt(req.getParameter("id"));
    NotaVenta n = notaDAO.obtener(idNota);
    if (n == null) { repartidoresHoy(req, res); return; }

    List<DetalleNotaVenta> det = detalleDAO.listarPorNota(idNota);

    List<InventarioDTO> inventario =
            distribucionDAO.inventarioPendiente(
                    n.getIdRepartidor(), LocalDate.now());

    req.setAttribute("nota",           n);
    req.setAttribute("detalle",        det);
    req.setAttribute("inventario",     inventario);
    req.setAttribute("inventarioJson", toJson(inventario));  // 🟢 JSON para editNota.js
    req.setAttribute("tiendas",        tiendaDAO.listarTodas());

    forwardVista(req, res, "jsp/notas/NotaFormEditar.jsp");
}


   /* ===== 5) Actualizar nota ===== */
private void actualizarNota(HttpServletRequest req,
                            HttpServletResponse res)
        throws SQLException, ServletException, IOException {

    int idNota   = Integer.parseInt(req.getParameter("id_nota"));
    int folio    = Integer.parseInt(req.getParameter("folio"));
    int idTienda = Integer.parseInt(req.getParameter("id_tienda"));

    NotaVenta n = notaDAO.obtener(idNota);
    if (n == null) { repartidoresHoy(req, res); return; }

    /* -- validar folio duplicado (si cambió) -- */
    if (n.getFolio() != folio && notaDAO.folioExiste(folio)) {
        req.getSession().setAttribute("flashMsg",
              "El folio&nbsp;<strong>"+folio+"</strong>&nbsp;ya existe");
        res.sendRedirect(req.getContextPath()
             + "/NotaVentaServlet?inFrame=1&accion=editarNota&id="+idNota);
        return;
    }

    /* ---------- 1. Revertir inventario anterior ---------- */
    List<DetalleNotaVenta> anteriores = detalleDAO.listarPorNota(idNota);
    for (DetalleNotaVenta d : anteriores) {
        int piezas = d.getCantidadVendida() + d.getMerma();
        invRepDAO.devolver(n.getIdRepartidor(), d.getIdEmpaque(), piezas);
        invGlobalDAO.regresarStockGeneral(d.getIdEmpaque(), piezas);
    }
    detalleDAO.eliminarPorNota(idNota);

    /* ---------- 2. Insertar nuevas líneas ---------- */
    List<DetalleNotaVenta> nuevos = parseDetalleJSON(req.getParameter("lineas"), idNota);
    for (DetalleNotaVenta d : nuevos) {
        detalleDAO.insertar(d);                                   // ⇦  INSERT sin total_linea
        int piezas = d.getCantidadVendida() + d.getMerma();
        invRepDAO.descontar(n.getIdRepartidor(), d.getIdEmpaque(), piezas);
        invGlobalDAO.registrarMovimientoSalida(
                d.getIdEmpaque(), piezas, d.getIdDistribucion(), n.getIdRepartidor());
    }

    /* ---------- 3. Actualizar encabezado ---------- */
    n.setFolio(folio);
    n.setIdTienda(idTienda);
    notaDAO.actualizar(n);

    /* ---------- 4. Recalcular TOTAL (usa total_linea generado) ---------- */
    notaDAO.actualizarTotal(idNota);               // ← realiza UPDATE con SUM(total_linea)

    /* ---------- 5. Redirige con mensaje flash ---------- */
    req.getSession().setAttribute("flashMsg", "Nota actualizada");
    res.sendRedirect(req.getContextPath()
            + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + n.getIdRepartidor());
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
        req.getSession().setAttribute("flashMsg", "Nota eliminada");
    res.sendRedirect(req.getContextPath()
            + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + n.getIdRepartidor());
    }

    /* ========== 7) Cerrar ruta ========== */
 
private void cerrarRuta(HttpServletRequest req,
                        HttpServletResponse res)
        throws SQLException, ServletException, IOException {

    int idRep = Integer.parseInt(req.getParameter("id_repartidor"));
    LocalDate hoy = LocalDate.now();

    /* --- devuelve al stock global todo el sobrante --- */
    List<InventarioDTO> pendiente =
            distribucionDAO.inventarioPendiente(idRep, hoy);

    for (InventarioDTO inv : pendiente) {
        if (inv.getRestante() > 0) {
            invGlobalDAO.regresarStockGeneral(
                    inv.getIdEmpaque(), inv.getRestante());

            // deja restante = 0 en inventario_repartidor
            invRepDAO.descontar(idRep,
                                inv.getIdEmpaque(),
                                -inv.getRestante());
        }
    }

    /* --- mensaje flash y redirección --- */
    req.getSession().setAttribute(
            "flashMsg", "Ruta cerrada y sobrante devuelto");

    res.sendRedirect(req.getContextPath()
            + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
}

    /* ===== helpers JSON & parsing ===== */
private String toJson(List<InventarioDTO> list){
    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < list.size(); i++) {
        InventarioDTO e = list.get(i);
        sb.append('"').append(e.getIdEmpaque()).append('"')   // <- comilla cerrada
          .append(":{")
          .append("\"nombre\":\"").append(e.getNombre()).append("\",")
          .append("\"precio\":").append(e.getPrecio()).append(',')
          .append("\"restante\":").append(e.getRestante()).append(',')
          .append("\"idDistribucion\":").append(e.getIdDistribucion())
          .append('}');
        if (i < list.size()-1) sb.append(',');
    }
    sb.append('}');
    return sb.toString();
}
    /**
     * Parsea JSON plano [{idEmpaque,idDistribucion,vendidas,merma,precio}]
     * → convierte a lista de DetalleNotaVenta.
     * Se deja sin implementación para compilar si no usas Gson todavía.
     */
  

/* ------------------------------------------------------------------
 * Convierte el JSON 'lineas' (string) a List<DetalleNotaVenta>
 * El JSON lo genera editNota.js, por ejemplo:
 * [ {idEmpaque:1,idDistribucion:3,vendidas:2,merma:0,precio:22.0}, … ]
 * ------------------------------------------------------------------ */
private List<DetalleNotaVenta> parseDetalleJSON(String json, int idNota) {
    List<DetalleNotaVenta> list = new ArrayList<>();
    if (json == null || json.isBlank()) return list;            // nada que procesar

    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

    for (JsonElement el : arr) {
        JsonObject o = el.getAsJsonObject();

        DetalleNotaVenta d = new DetalleNotaVenta();
        d.setIdNota(idNota);

        d.setIdEmpaque( safeInt(o, "idEmpaque") );              // ← siempre existe
        d.setIdDistribucion( safeInt(o, "idDistribucion") );    // ← puede venir null
        d.setCantidadVendida( safeInt(o, "vendidas") );
        d.setMerma( safeInt(o, "merma") );
        d.setPrecioUnitario( safeDouble(o, "precio") );

        list.add(d);
    }
    return list;
}

/* ---------- helpers que manejan JsonNull ---------- */
private int safeInt(JsonObject o, String k){
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0;
}
private double safeDouble(JsonObject o, String k){
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsDouble() : 0.0;
}
    
        
}
