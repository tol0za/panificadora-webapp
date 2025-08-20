package servlet;

import dao.*;
import dto.DistribucionResumen;
import dto.InventarioDTO;
import modelo.DetalleNotaVenta;
import modelo.NotaVenta;

import com.google.gson.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

/** Controlador principal del módulo de Notas de Venta */
@WebServlet(name="NotaVentaServlet", urlPatterns={"/NotaVentaServlet"})
public class NotaVentaServlet extends HttpServlet {

    /* ============ DAOs ============ */
    private NotaVentaDAO             notaDAO;
    private DetalleNotaDAO           detalleDAO;
    private InventarioRepartidorDAO  invRepDAO;
    private InventarioEmpaquetadoDAO invGlobalDAO;   // stock global
    private DistribucionDAO          distribucionDAO;
    private RepartidorDAO            repartidorDAO;
    private TiendaDAO                tiendaDAO;
    private CatalogoEmpaqueDAO       empaqueDAO;
    private RutaCierreDAO            rutaDAO;

    /* ============ Acciones ============ */
    private static final String ACC_REPARTIDORES      = "repartidores";
    private static final String ACC_VISTA_REPARTIDOR  = "vistaRepartidor";
    private static final String ACC_GUARDAR           = "guardarNota";
    private static final String ACC_EDITAR_FORM       = "editarNota";
    private static final String ACC_ACTUALIZAR        = "actualizarNota";
    private static final String ACC_ELIMINAR          = "eliminarNota";
    private static final String ACC_CERRAR_RUTA       = "cerrarRuta";
    private static final String ACC_REABRIR_RUTA      = "reabrirRuta";
    private static final String ACC_FOLIO_CHECK       = "folioCheck";
    private static final String ACC_DETALLE_JSON      = "detalleJson";
    private static final String ACC_IMPRIMIR_DIA      = "imprimirNotasDia";
    private static final String ACC_HISTORIAL         = "historial";
    private static final String ACC_HIST_BUSCAR       = "histBuscar";

    @Override public void init() throws ServletException {
        try {
            notaDAO         = new NotaVentaDAO();
            detalleDAO      = new DetalleNotaDAO();
            invRepDAO       = new InventarioRepartidorDAO();
            invGlobalDAO    = new InventarioEmpaquetadoDAO();
            distribucionDAO = new DistribucionDAO();
            repartidorDAO   = new RepartidorDAO();
            tiendaDAO       = new TiendaDAO();
            empaqueDAO      = new CatalogoEmpaqueDAO();
            rutaDAO         = new RutaCierreDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al inicializar DAOs", e);
        }
    }

    /* -------- helpers iframe -------- */
    private boolean fromIframe(HttpServletRequest req){
        if ("1".equals(req.getParameter("inFrame"))) return true;
        String ref = req.getHeader("referer");
        return ref != null && ref.contains("/jsp/home/inicio.jsp");
    }
    private void forwardVista(HttpServletRequest req,HttpServletResponse res,String vista)
            throws ServletException,IOException{
        if(!fromIframe(req))
            res.sendRedirect(req.getContextPath()+"/jsp/home/inicio.jsp?vista="+vista);
        else
            req.getRequestDispatcher("/"+vista).forward(req,res);
    }

    /* -------------- entry ------------- */
    @Override protected void doGet (HttpServletRequest r,HttpServletResponse s) throws ServletException,IOException { process(r,s); }
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s) throws ServletException,IOException { process(r,s); }

    private void process(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        /* ---------- AJAX 1: folio duplicado ---------- */
        if (ACC_FOLIO_CHECK.equals(req.getParameter("accion"))) {
            boolean duplicado;
            try {
                int f = Integer.parseInt(req.getParameter("folio"));
                duplicado = notaDAO.folioExiste(f);
            } catch (Exception e) { duplicado = true; }
            res.setContentType("text/plain");
            res.getWriter().print(duplicado ? "1" : "0");
            return;
        }
        /* ---------- AJAX 2: detalle JSON ---------- */
        if (ACC_DETALLE_JSON.equals(req.getParameter("accion"))) {
            try {
                int idNota = Integer.parseInt(req.getParameter("id"));
                List<DetalleNotaVenta> det = detalleDAO.listarPorNota(idNota);
                for (DetalleNotaVenta d : det) {
                    d.setNombreEmpaque(empaqueDAO.buscarPorId(d.getIdEmpaque()).getNombreEmpaque());
                }
                res.setContentType("application/json");
                res.getWriter().print(new Gson().toJson(det));
            } catch (Exception e){
                res.sendError(500, "Error obteniendo detalle");
            }
            return;
        }

        /* ---------------- flujo principal ---------------- */
        String acc = req.getParameter("accion");
        if (acc == null) acc = ACC_REPARTIDORES;

        try {
            switch (acc) {
                case ACC_REPARTIDORES      -> repartidoresHoy(req,res);
                case ACC_VISTA_REPARTIDOR  -> vistaRepartidor(req,res);
                case ACC_GUARDAR           -> guardarNota(req,res);
                case ACC_EDITAR_FORM       -> editarForm(req,res);
                case ACC_ACTUALIZAR        -> actualizarNota(req,res);
                case ACC_ELIMINAR          -> eliminarNota(req,res);
                case ACC_CERRAR_RUTA       -> cerrarRuta(req,res);
                case ACC_REABRIR_RUTA      -> reabrirRuta(req,res);
                case ACC_IMPRIMIR_DIA      -> imprimirNotasDia(req,res);
                case ACC_HISTORIAL         -> historial(req,res);
                case ACC_HIST_BUSCAR       -> histBuscar(req,res);
                default                    -> repartidoresHoy(req,res);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /* ==============================================================
     * 1. Repartidores con salida hoy
     * ============================================================ */
    private void repartidoresHoy(HttpServletRequest req,HttpServletResponse res)
          throws SQLException,ServletException,IOException{
        LocalDate hoy = LocalDate.now();
        List<DistribucionResumen> lista = distribucionDAO.repartidoresConSalida(hoy);

        Map<Integer,Integer> notasPorRep = new HashMap<>();
        Map<Integer,Double>  totalPorRep = new HashMap<>();
        for (DistribucionResumen dr : lista) {
            int idRep = dr.getIdRepartidor();
            int cnt   = notaDAO.contarPorRepartidorYFecha(idRep, hoy);
            double tot= notaDAO.getTotalDia(idRep, hoy); // cobradas
            notasPorRep.put(idRep, cnt);
            totalPorRep.put(idRep, tot);
        }
        req.setAttribute("listaRepartidores", lista);
        req.setAttribute("hoy", hoy);
        req.setAttribute("notasPorRep", notasPorRep);
        req.setAttribute("totalPorRep", totalPorRep);
        forwardVista(req,res,"jsp/notas/RepartidoresConSalidaHoy.jsp");
    }

    /* =============================================================
     * 2. Vista del día para un repartidor
     * =========================================================== */
    private void vistaRepartidor(HttpServletRequest req,HttpServletResponse res)
            throws SQLException,ServletException,IOException{
        int idRep = Integer.parseInt(req.getParameter("id"));
        LocalDate hoy = LocalDate.now();
        List<InventarioDTO> inventario = distribucionDAO.inventarioPendiente(idRep, hoy);
        List<NotaVenta>     notas      = notaDAO.listarPorRepartidorYFecha(idRep, hoy);

        double totalDia = 0;
        for (NotaVenta n : notas) {
            n.setNombreTienda(tiendaDAO.buscarPorId(n.getIdTienda()).getNombre());
            double tot = detalleDAO.obtenerTotalPorNota(n.getIdNotaVenta()); // cobradas
            n.setTotal(tot);
            totalDia += tot;
        }
        req.setAttribute("inventario",     inventario);
        req.setAttribute("inventarioJson", toJson(inventario));
        req.setAttribute("listaNotas",     notas);
        req.setAttribute("totalDia",       totalDia);
        req.setAttribute("repartidor",     repartidorDAO.obtener(idRep));
        req.setAttribute("tiendas",        tiendaDAO.listarTodas());
        req.setAttribute("hoy",            hoy);
        boolean rutaCerrada = rutaDAO.estaCerrada(idRep, hoy);
        req.setAttribute("rutaCerrada", rutaCerrada);
        forwardVista(req,res,"jsp/notas/NotaDiaRepartidor.jsp");
    }

    /* =============================================================
     * 3. Guardar nota
     * =========================================================== */
    /* =============================================================
 * 3. Guardar nota  (con VALIDACIÓN de stock por repartidor/empaque)
 * =========================================================== */
private void guardarNota(HttpServletRequest req,
                         HttpServletResponse res)
        throws SQLException, ServletException, IOException {
    int folio     = Integer.parseInt(req.getParameter("folio"));
    int idRep     = Integer.parseInt(req.getParameter("id_repartidor"));
    int idTienda  = Integer.parseInt(req.getParameter("id_tienda"));

    if (rutaDAO.estaCerrada(idRep, LocalDate.now())) {
        req.getSession().setAttribute("flashMsg", "Ruta cerrada. Reábrela para agregar notas.");
        res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
        return;
    }
    if (notaDAO.folioExiste(folio)) {
        req.getSession().setAttribute("flashMsg","El folio <strong>" + folio + "</strong> ya existe");
        res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
        return;
    }

    NotaVenta n = new NotaVenta();
    n.setFolio(folio);
    n.setIdRepartidor(idRep);
    n.setIdTienda(idTienda);
    n.setFechaNota(LocalDateTime.now());
    n.setTotal(0);
    int idNota = notaDAO.insertar(n);

    // Líneas capturadas (del hidden #lineas). El parser ya tolera snake/camel.
    List<DetalleNotaVenta> lineas = parseDetalleJSON(req.getParameter("lineas"), idNota);

    // === VALIDACIÓN DE STOCK (crear) ===
    // En nueva nota, permitido = restante_actual; uso = vendidas (no cuenta merma)
    Map<Integer,Integer> porEmpaqueVendidas = new HashMap<>();
    for (DetalleNotaVenta d : lineas) {
        if (d.getMerma() > d.getCantidadVendida()) {
            req.getSession().setAttribute("flashMsg","Merma no puede ser mayor que vendidas (empaque ID " + d.getIdEmpaque() + ")");
            res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
            return;
        }
        porEmpaqueVendidas.merge(d.getIdEmpaque(), d.getCantidadVendida(), Integer::sum);
    }
    for (var e : porEmpaqueVendidas.entrySet()) {
        int idEmp = e.getKey();
        int vend  = e.getValue();
        int restante = invRepDAO.getRestante(idRep, idEmp);
        if (vend > restante) {
            req.getSession().setAttribute("flashMsg",
                "<strong>Stock insuficiente</strong>: Repartidor " + idRep + ", Empaque " + idEmp +
                ". Pedidas (vendidas) = " + vend + ", Restante = " + restante);
            res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
            return;
        }
    }

    // === APLICAR ===
    try {
        for (DetalleNotaVenta d : lineas) {
            int piezasVendidas = d.getCantidadVendida();           // SOLO vendidas
            invRepDAO.descontar(idRep, d.getIdEmpaque(), piezasVendidas); // valida stock
            detalleDAO.insertar(d);                                // total_linea lo calcula la BD (columna generada)
        }
    } catch (SQLException ex) {
        notaDAO.eliminar(idNota);
        req.getSession().setAttribute("flashMsg",
            "<strong>Error</strong>: " + ex.getMessage());
        res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
        return;
    }

    // Recalcula total (con cobradas) y persiste
    notaDAO.actualizarTotal(idNota);
    req.getSession().setAttribute("flashMsg", "Nota guardada");
    res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
}


    /* =============================================================
     * 4. Form editar
     * =========================================================== */
    private void editarForm(HttpServletRequest req,HttpServletResponse res)
            throws SQLException,ServletException,IOException{
        int idNota = Integer.parseInt(req.getParameter("id"));
        NotaVenta n = notaDAO.obtener(idNota);
        if (n == null){ repartidoresHoy(req,res); return; }

        List<DetalleNotaVenta> det = detalleDAO.listarPorNota(idNota);
        for (DetalleNotaVenta d : det) {
            d.setNombreEmpaque(empaqueDAO.buscarPorId(d.getIdEmpaque()).getNombreEmpaque());
        }
        LocalDate hoy = LocalDate.now();
        List<InventarioDTO> invPend = distribucionDAO.inventarioPendiente(n.getIdRepartidor(), hoy);

        req.setAttribute("nota",           n);
        req.setAttribute("detalle",        det);
        req.setAttribute("inventario",     invPend);
        req.setAttribute("inventarioJson", toJson(invPend));
        req.setAttribute("tiendas",        tiendaDAO.listarTodas());
        forwardVista(req,res,"jsp/notas/NotaFormEditar.jsp");
    }

    /* =============================================================
     * 5. Actualizar nota (reversa y aplica)
     * =========================================================== */
    
private void actualizarNota(HttpServletRequest req,
                            HttpServletResponse res)
        throws SQLException, ServletException, IOException {
    int idNota   = Integer.parseInt(req.getParameter("id_nota"));
    int folio    = Integer.parseInt(req.getParameter("folio"));
    int idTienda = Integer.parseInt(req.getParameter("id_tienda"));

    NotaVenta n = notaDAO.obtener(idNota);
    if (n == null) { repartidoresHoy(req, res); return; }

    if (rutaDAO.estaCerrada(n.getIdRepartidor(), LocalDate.now())) {
        req.getSession().setAttribute("flashMsg", "Ruta cerrada. Reábrela para editar notas.");
        res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=editarNota&id=" + idNota);
        return;
    }
    if (n.getFolio() != folio && notaDAO.folioExiste(folio)) {
        req.getSession().setAttribute("flashMsg","El folio <strong>" + folio + "</strong> ya existe");
        res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=editarNota&id=" + idNota);
        return;
    }

    // Nuevas líneas propuestas
    List<DetalleNotaVenta> nuevas = parseDetalleJSON(req.getParameter("lineas"), idNota);

    // === VALIDACIÓN DE STOCK (editar) ===
    // Permitido por empaque = restante_actual + vendidas_originales_de_esta_nota
    // Uso por empaque = nuevas_vendidas (merma NO descuenta stock)
    Map<Integer,Integer> prevVendidasByEmp = new HashMap<>();
    for (DetalleNotaVenta dPrev : detalleDAO.listarPorNota(idNota)) {
        prevVendidasByEmp.merge(dPrev.getIdEmpaque(), dPrev.getCantidadVendida(), Integer::sum);
    }

    Map<Integer,Integer> nuevasVendidasByEmp = new HashMap<>();
    for (DetalleNotaVenta d : nuevas) {
        // En edición, UI de "vendidas" muestra cobradas; parser ya convierte a BRUTAS; aquí validamos SOLO vendidas
        if (d.getMerma() > d.getCantidadVendida()) {
            req.getSession().setAttribute("flashMsg","Merma no puede ser mayor que vendidas (empaque ID " + d.getIdEmpaque() + ")");
            res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=editarNota&id=" + idNota);
            return;
        }
        nuevasVendidasByEmp.merge(d.getIdEmpaque(), d.getCantidadVendida(), Integer::sum);
    }

    for (var e : nuevasVendidasByEmp.entrySet()) {
        int idEmp = e.getKey();
        int nuevasVend = e.getValue();
        int restante   = invRepDAO.getRestante(n.getIdRepartidor(), idEmp);
        int prevVend   = prevVendidasByEmp.getOrDefault(idEmp, 0);
        int permitido  = restante + prevVend;    // puede reusar lo que ya tenía la nota
        if (nuevasVend > permitido) {
            req.getSession().setAttribute("flashMsg",
                "<strong>Stock insuficiente</strong>: Empaque " + idEmp +
                ". Vendidas nuevas=" + nuevasVend + ", Permitido=" + permitido +
                " (Restante=" + restante + ", Previas=" + prevVend + ")");
            res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=editarNota&id=" + idNota);
            return;
        }
    }

    // === REVERTIR + APLICAR ===
    // 1) Revertir SOLO vendidas anteriores
    for (DetalleNotaVenta d : detalleDAO.listarPorNota(idNota)) {
        invRepDAO.devolver(n.getIdRepartidor(), d.getIdEmpaque(), d.getCantidadVendida());
    }

    // 2) Reemplazar detalle y descontar SOLO nuevas vendidas
    detalleDAO.eliminarPorNota(idNota);
    try {
        for (DetalleNotaVenta d : nuevas) {
            invRepDAO.descontar(n.getIdRepartidor(), d.getIdEmpaque(), d.getCantidadVendida());
            detalleDAO.insertar(d); // total_linea lo calcula la BD
        }
    } catch (SQLException ex) {
        req.getSession().setAttribute("flashMsg",
            "<strong>Error</strong>: " + ex.getMessage());
        res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=editarNota&id=" + idNota);
        return;
    }

    // 3) Cabecera y total
    n.setFolio(folio);
    n.setIdTienda(idTienda);
    notaDAO.actualizar(n);
    notaDAO.actualizarTotal(idNota);

    req.getSession().setAttribute("flashMsg", "Nota actualizada");
    res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + n.getIdRepartidor());
}
    /* =============================================================
     * 6. Eliminar nota
     * =========================================================== */
    private void eliminarNota(HttpServletRequest req,HttpServletResponse res)
            throws SQLException,ServletException,IOException{
        int idNota = Integer.parseInt(req.getParameter("id"));
        NotaVenta n = notaDAO.obtener(idNota);
        if (n == null){ repartidoresHoy(req,res); return; }

        if (rutaDAO.estaCerrada(n.getIdRepartidor(), LocalDate.now())) {
            req.getSession().setAttribute("flashMsg","Ruta cerrada. Reábrela para eliminar notas.");
            res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id="+n.getIdRepartidor());
            return;
        }

        // *** Devolver SOLO lo descontado antes: "vendidas"
        for (DetalleNotaVenta d : detalleDAO.listarPorNota(idNota)) {
            int piezas = d.getCantidadVendida();
            invRepDAO.devolver(n.getIdRepartidor(), d.getIdEmpaque(), piezas);
        }
        detalleDAO.eliminarPorNota(idNota);
        notaDAO.eliminar(idNota);

        req.getSession().setAttribute("flashMsg", "Nota eliminada");
        res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id="+n.getIdRepartidor());
    }

    /* =============================================================
     * 7. Cerrar ruta
     * =========================================================== */
    private void cerrarRuta(HttpServletRequest req,
                            HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        int       idRep = Integer.parseInt(req.getParameter("id_repartidor"));
        LocalDate hoy   = LocalDate.now();

        Map<Integer,Integer> mapSobrante = new HashMap<>();
        for (InventarioDTO inv : distribucionDAO.inventarioPendiente(idRep, hoy)) {
            mapSobrante.merge(inv.getIdEmpaque(), inv.getRestante(), Integer::sum);
        }
        for (var e : mapSobrante.entrySet()) {
            int idEmp  = e.getKey();
            int piezas = e.getValue();
            if (piezas == 0) continue;
            invGlobalDAO.regresarStockGeneral(idEmp, piezas, idRep); // ENTRADA_RETORNO
            invRepDAO.consumirRestante(idRep, idEmp, piezas);        // dejar en 0
        }
        rutaDAO.cerrar(idRep, hoy);
        req.getSession().setAttribute("flashMsg", "Ruta cerrada y sobrante devuelto");
        res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
    }

    /* =============================================================
     * 8. Reabrir ruta
     * =========================================================== */
    private void reabrirRuta(HttpServletRequest req,
                             HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        int       idRep = Integer.parseInt(req.getParameter("id_repartidor"));
        LocalDate hoy   = LocalDate.now();

        Map<Integer,Integer> devoluciones = invGlobalDAO.obtenerRetornoPorRepartidorYFecha(idRep, hoy); // <idEmp, piezas>
        for (var e : devoluciones.entrySet()) {
            int idEmp   = e.getKey();
            int piezas  = e.getValue();
            if (piezas == 0) continue;
            invGlobalDAO.registrarMovimientoSalida(idEmp, piezas, 0, idRep, "REABRIR_RUTA");
            invRepDAO.insertarInicial(idRep, idEmp, piezas);
        }
        rutaDAO.reabrir(idRep, hoy);
        req.getSession().setAttribute("flashMsg","Ruta reabierta: puedes capturar notas.");
        res.sendRedirect(req.getContextPath()+"/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
    }

    /* =============================================================
     * 9. Historial por fechas
     * =========================================================== */
    private void historial(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        LocalDate hoy = LocalDate.now();
        req.setAttribute("desde", hoy.minusDays(6).toString());
        req.setAttribute("hasta", hoy.toString());
        forwardVista(req, res, "jsp/notas/historialNotas.jsp");
    }
    private void histBuscar(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException, SQLException {
        LocalDate desde = parseOr(LocalDate.now().minusDays(6), req.getParameter("desde"));
        LocalDate hasta = parseOr(LocalDate.now(),              req.getParameter("hasta"));
        var lista  = notaDAO.listarPorRango(desde, hasta);           // totales con cobradas
        var diario = notaDAO.resumenDiario(desde, hasta);            // idem
        var reps   = notaDAO.resumenPorRepartidor(desde, hasta);     // idem
        req.setAttribute("desde", desde.toString());
        req.setAttribute("hasta", hasta.toString());
        req.setAttribute("listaNotas",    lista);
        req.setAttribute("resumenDiario", diario);
        req.setAttribute("resumenReps",   reps);
        forwardVista(req, res, "jsp/notas/historialNotas.jsp");
    }
    private LocalDate parseOr(LocalDate fallback, String s){
        try { return (s==null || s.isBlank()) ? fallback : LocalDate.parse(s); }
        catch (Exception e){ return fallback; }
    }

    /* =============================================================
     * 10. Imprimir todas las notas del día (PDF)
     * =========================================================== */
    private void imprimirNotasDia(HttpServletRequest req,
                                  HttpServletResponse  res)
            throws ServletException, IOException {
        String repStr = req.getParameter("id_repartidor");
        if (repStr == null || repStr.isBlank()) {
            res.sendError(400, "Falta parámetro id_repartidor");
            return;
        }
        int idRep = Integer.parseInt(repStr);
        try {
            LocalDate hoy = LocalDate.now();
            List<NotaVenta> notas = notaDAO.listarPorRepartidorYFecha(idRep, hoy);
            if (notas.isEmpty()) {
                res.sendError(404, "No hay notas para imprimir");
                return;
            }
            res.setContentType("application/pdf");
            res.setHeader("Content-Disposition",
                          "inline; filename=notas_rep_" + idRep + "_" + hoy + ".pdf");

            PdfWriter   wr  = new PdfWriter(res.getOutputStream());
            PdfDocument pdf = new PdfDocument(wr);
            Document    doc = new Document(pdf);

            Image logo = new Image(ImageDataFactory.create(
                    req.getServletContext().getRealPath("/static/img/logo_pdf.png")))
                    .scaleToFit(90, 90);

            Paragraph datosEmp = new Paragraph()
                    .add("PANIFICADORA DEL VALLE\n")
                    .add("RFC TOHL841101PZ6\n")
                    .add("Calle Guztavo A Vallejo\n")
                    .add("San Quintín BC\n")
                    .add("Tel. 616-136-7253")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBold().setMargin(0);

            Table cab = new Table(new float[]{1, 3}).useAllAvailableWidth().setBorder(Border.NO_BORDER);
            cab.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
            cab.addCell(new Cell().add(datosEmp).setBorder(Border.NO_BORDER));
            doc.add(cab).add(new Paragraph("\n"));

            double granTotal = 0;

            for (NotaVenta nota : notas) {
                doc.add(new Paragraph("NOTA DE VENTA  #" + nota.getFolio())
                        .setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());

                doc.add(new Paragraph()
                        .add("Tienda: ")
                        .add(tiendaDAO.buscarPorId(nota.getIdTienda()).getNombre())
                        .add("\nFecha: " + nota.getFechaNota())
                        .setMarginBottom(6));

                List<DetalleNotaVenta> det = detalleDAO.listarPorNota(nota.getIdNotaVenta());
                for (DetalleNotaVenta d : det) {
                    d.setNombreEmpaque(empaqueDAO.buscarPorId(d.getIdEmpaque()).getNombreEmpaque());
                }

                Table tbl = new Table(new float[]{5,2,2,2}).useAllAvailableWidth();
                tbl.addHeaderCell(header("Empaque"));
                tbl.addHeaderCell(header("Cobradas"));
                tbl.addHeaderCell(header("Merma"));
                tbl.addHeaderCell(header("Subtotal"));

                double total = 0;
                for (DetalleNotaVenta d : det) {
                    int cobradas = Math.max(0, d.getCantidadVendida() - d.getMerma());  // ***
                    double sub   = cobradas * d.getPrecioUnitario();                    // ***
                    tbl.addCell(cell(d.getNombreEmpaque()));
                    tbl.addCell(cell(cobradas));
                    tbl.addCell(cell(d.getMerma()));
                    tbl.addCell(cell(String.format("$ %.2f", sub)));
                    total += sub;
                }
                doc.add(tbl);
                doc.add(new Paragraph("Total nota: $ " + String.format("%.2f", total))
                        .setTextAlignment(TextAlignment.RIGHT).setBold().setMarginBottom(10));
                granTotal += total;

                if (nota != notas.get(notas.size()-1)) {
                    doc.add(new Paragraph("\n").setBorderTop(new SolidBorder(0.5f)));
                }
            }

            doc.add(new Paragraph("TOTAL DEL DÍA: $ " + String.format("%.2f", granTotal))
                    .setTextAlignment(TextAlignment.RIGHT).setFontSize(13).setBold().setMarginTop(15));
            doc.close();

        } catch (Exception e) {
            throw new ServletException("No se pudo generar el PDF", e);
        }
    }

    /* Helpers PDF */
    private Cell header(String txt){ return new Cell().add(new Paragraph(txt)).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBold(); }
    private Cell cell(Object txt){ return new Cell().add(new Paragraph(String.valueOf(txt))); }

    /* =============================================================
     * helpers JSON & parser tolerante a snake/camel
     * =========================================================== */
    private String toJson(List<InventarioDTO> list){
        StringBuilder sb = new StringBuilder("{");
        for (int i=0;i<list.size();i++){
            InventarioDTO e = list.get(i);
            sb.append('"').append(e.getIdEmpaque()).append('"')
              .append(":{")
              .append("\"nombre\":\"").append(e.getNombre()).append("\",")
              .append("\"precio\":").append(e.getPrecio()).append(',')
              .append("\"restante\":").append(e.getRestante()).append(',')
              .append("\"idDistribucion\":").append(e.getIdDistribucion())
              .append('}');
            if (i<list.size()-1) sb.append(',');
        }
        sb.append('}');
        return sb.toString();
    }

    /** Parser robusto para líneas: acepta snake_case o camelCase */
    private List<DetalleNotaVenta> parseDetalleJSON(String json, int idNota){
        List<DetalleNotaVenta> list = new ArrayList<>();
        if (json == null || json.isBlank()) return list;

        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement el : arr){
            JsonObject o = el.getAsJsonObject();
            DetalleNotaVenta d = new DetalleNotaVenta();
            d.setIdNota(idNota);

            int idEmp = pickInt(o, "idEmpaque","id_empaque","emp","idEmp");
            int idDis = pickInt(o, "idDistribucion","id_distribucion","idDist","dist");
            int vend  = pickInt(o, "vendidas","cantidadVendida","cobradasYMerma","vend");
            int merma = pickInt(o, "merma","cantidadMerma","m");
            double pu = pickDouble(o, "precio","precio_unitario","pu");

            d.setIdEmpaque(idEmp);
            d.setIdDistribucion(idDis);
            d.setCantidadVendida(vend); // vendidas = cobradas + merma
            d.setMerma(merma);
            d.setPrecioUnitario(pu);

            list.add(d);
        }
        return list;
    }
    private int pickInt(JsonObject o, String... keys){
        for (String k: keys){
            if (o.has(k) && !o.get(k).isJsonNull()){
                try { return o.get(k).getAsInt(); } catch (Exception ignore){}
            }
        }
        return 0;
    }
    private double pickDouble(JsonObject o, String... keys){
        for (String k: keys){
            if (o.has(k) && !o.get(k).isJsonNull()){
                try { return o.get(k).getAsDouble(); } catch (Exception ignore){}
            }
        }
        return 0.0;
    }
}
