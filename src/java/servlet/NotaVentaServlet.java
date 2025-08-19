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
import com.itextpdf.layout.properties.VerticalAlignment;

/** Controlador principal del módulo de Notas de Venta */

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
    private RutaCierreDAO            rutaDAO;        // ← NUEVO

    /* ============ Acciones ============ */
    private static final String ACC_REPARTIDORES      = "repartidores";
    private static final String ACC_VISTA_REPARTIDOR  = "vistaRepartidor";
    private static final String ACC_GUARDAR           = "guardarNota";
    private static final String ACC_EDITAR_FORM       = "editarNota";
    private static final String ACC_ACTUALIZAR        = "actualizarNota";
    private static final String ACC_ELIMINAR          = "eliminarNota";
    private static final String ACC_CERRAR_RUTA       = "cerrarRuta";
    private static final String ACC_REABRIR_RUTA      = "reabrirRuta";        // ← NUEVO
    private static final String ACC_FOLIO_CHECK       = "folioCheck";
    private static final String ACC_DETALLE_JSON      = "detalleJson";
    private static final String ACC_IMPRIMIR_DIA      = "imprimirNotasDia";   // ← NUEVO

    // Historial por fechas (NUEVO)
    private static final String ACC_HISTORIAL         = "historial";
    private static final String ACC_HIST_BUSCAR       = "histBuscar";

    /* ============ init ============ */
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
            rutaDAO         = new RutaCierreDAO(); // ← NUEVO
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
                    d.setNombreEmpaque(
                            empaqueDAO.buscarPorId(d.getIdEmpaque()).getNombreEmpaque());
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

                // Historial por fechas (NUEVO)
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
        req.setAttribute("listaRepartidores", lista);
        req.setAttribute("hoy", hoy);
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
            double tot = detalleDAO.obtenerTotalPorNota(n.getIdNotaVenta());
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

        boolean rutaCerrada = rutaDAO.estaCerrada(idRep, hoy);   // ← NUEVO
        req.setAttribute("rutaCerrada", rutaCerrada);

        forwardVista(req,res,"jsp/notas/NotaDiaRepartidor.jsp");
    }

    /* =============================================================
     * 3. Guardar nota
     * =========================================================== */
    private void guardarNota(HttpServletRequest req,
                             HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        int folio     = Integer.parseInt(req.getParameter("folio"));
        int idRep     = Integer.parseInt(req.getParameter("id_repartidor"));
        int idTienda  = Integer.parseInt(req.getParameter("id_tienda"));

        if (rutaDAO.estaCerrada(idRep, LocalDate.now())) {            // ← NUEVO
            req.getSession().setAttribute("flashMsg", "Ruta cerrada. Reábrela para agregar notas.");
            res.sendRedirect(req.getContextPath()
                    + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
            return;
        }

        if (notaDAO.folioExiste(folio)) {
            req.getSession().setAttribute("flashMsg",
                "El folio <strong>" + folio + "</strong> ya existe");
            res.sendRedirect(req.getContextPath()
                + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
            return;
        }

        NotaVenta n = new NotaVenta();
        n.setFolio(folio);
        n.setIdRepartidor(idRep);
        n.setIdTienda(idTienda);
        n.setFechaNota(LocalDateTime.now());
        n.setTotal(0);
        int idNota = notaDAO.insertar(n);

        List<DetalleNotaVenta> lineas =
                parseDetalleJSON(req.getParameter("lineas"), idNota);
        try {
            for (DetalleNotaVenta d : lineas) {
                int piezas = d.getCantidadVendida() + d.getMerma();
                invRepDAO.descontar(idRep, d.getIdEmpaque(), piezas);  // valida stock
                detalleDAO.insertar(d);
            }
        } catch (SQLException ex) {
            notaDAO.eliminar(idNota);
            req.getSession().setAttribute("flashMsg",
                "<strong>Stock insuficiente</strong>: " + ex.getMessage());
            res.sendRedirect(req.getContextPath()
                + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
            return;
        }

        notaDAO.actualizarTotal(idNota);
        req.getSession().setAttribute("flashMsg", "Nota guardada");
        res.sendRedirect(req.getContextPath()
            + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
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
            d.setNombreEmpaque(
                    empaqueDAO.buscarPorId(d.getIdEmpaque()).getNombreEmpaque());
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
     * 5. Actualizar nota
     * =========================================================== */
    private void actualizarNota(HttpServletRequest req,
                                HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        int idNota   = Integer.parseInt(req.getParameter("id_nota"));
        int folio    = Integer.parseInt(req.getParameter("folio"));
        int idTienda = Integer.parseInt(req.getParameter("id_tienda"));

        NotaVenta n = notaDAO.obtener(idNota);
        if (n == null) { repartidoresHoy(req, res); return; }

        if (rutaDAO.estaCerrada(n.getIdRepartidor(), LocalDate.now())) {   // ← NUEVO
            req.getSession().setAttribute("flashMsg", "Ruta cerrada. Reábrela para editar notas.");
            res.sendRedirect(req.getContextPath()
                + "/NotaVentaServlet?inFrame=1&accion=editarNota&id=" + idNota);
            return;
        }

        if (n.getFolio() != folio && notaDAO.folioExiste(folio)) {
            req.getSession().setAttribute("flashMsg",
                "El folio <strong>" + folio + "</strong> ya existe");
            res.sendRedirect(req.getContextPath()
                + "/NotaVentaServlet?inFrame=1&accion=editarNota&id=" + idNota);
            return;
        }

        // Revertir inventario anterior SOLO al repartidor
        for (DetalleNotaVenta d : detalleDAO.listarPorNota(idNota)) {
            int piezas = d.getCantidadVendida() + d.getMerma();
            invRepDAO.devolver(n.getIdRepartidor(), d.getIdEmpaque(), piezas);
        }
        detalleDAO.eliminarPorNota(idNota);

        try {
            for (DetalleNotaVenta d
                    : parseDetalleJSON(req.getParameter("lineas"), idNota)) {
                int piezas = d.getCantidadVendida() + d.getMerma();
                invRepDAO.descontar(n.getIdRepartidor(), d.getIdEmpaque(), piezas);
                detalleDAO.insertar(d);
            }
        } catch (SQLException ex) {
            req.getSession().setAttribute("flashMsg",
                "<strong>Stock insuficiente</strong>: " + ex.getMessage());
            res.sendRedirect(req.getContextPath()
                + "/NotaVentaServlet?inFrame=1&accion=editarNota&id=" + idNota);
            return;
        }

        n.setFolio(folio);
        n.setIdTienda(idTienda);
        notaDAO.actualizar(n);
        notaDAO.actualizarTotal(idNota);

        req.getSession().setAttribute("flashMsg", "Nota actualizada");
        res.sendRedirect(req.getContextPath()
            + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + n.getIdRepartidor());
    }

    /* =============================================================
     * 6. Eliminar nota
     * =========================================================== */
    private void eliminarNota(HttpServletRequest req,HttpServletResponse res)
            throws SQLException,ServletException,IOException{
        int idNota = Integer.parseInt(req.getParameter("id"));
        NotaVenta n = notaDAO.obtener(idNota);
        if (n == null){ repartidoresHoy(req,res); return; }

        if (rutaDAO.estaCerrada(n.getIdRepartidor(), LocalDate.now())) {   // ← NUEVO
            req.getSession().setAttribute("flashMsg",
                    "Ruta cerrada. Reábrela para eliminar notas.");
            res.sendRedirect(req.getContextPath()
                    + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id="+n.getIdRepartidor());
            return;
        }

        for (DetalleNotaVenta d : detalleDAO.listarPorNota(idNota)) {
            int piezas = d.getCantidadVendida() + d.getMerma();
            invRepDAO.devolver(n.getIdRepartidor(), d.getIdEmpaque(), piezas);
        }
        detalleDAO.eliminarPorNota(idNota);
        notaDAO.eliminar(idNota);

        req.getSession().setAttribute("flashMsg", "Nota eliminada");
        res.sendRedirect(req.getContextPath()
                + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id="+n.getIdRepartidor());
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

        rutaDAO.cerrar(idRep, hoy);                            // ← NUEVO
        req.getSession().setAttribute("flashMsg", "Ruta cerrada y sobrante devuelto");
        res.sendRedirect(req.getContextPath()
                + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
    }

    /* =============================================================
     * 8. Reabrir ruta (para capturar notas faltantes)
     * =========================================================== */
    private void reabrirRuta(HttpServletRequest req,
                             HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        int       idRep = Integer.parseInt(req.getParameter("id_repartidor"));
        LocalDate hoy   = LocalDate.now();

        Map<Integer,Integer> devoluciones =
                invGlobalDAO.obtenerRetornoPorRepartidorYFecha(idRep, hoy); // <idEmp, piezas>

        for (var e : devoluciones.entrySet()) {
            int idEmp   = e.getKey();
            int piezas  = e.getValue();
            if (piezas == 0) continue;
            invGlobalDAO.registrarMovimientoSalida(idEmp, piezas, 0, idRep, "REABRIR_RUTA");
            invRepDAO.insertarInicial(idRep, idEmp, piezas);
        }

        rutaDAO.reabrir(idRep, hoy);                           // ← NUEVO
        req.getSession().setAttribute("flashMsg","Ruta reabierta: puedes capturar notas.");
        res.sendRedirect(req.getContextPath()
                + "/NotaVentaServlet?inFrame=1&accion=vistaRepartidor&id=" + idRep);
    }

    /* =============================================================
     * 9. Historial por fechas (NUEVO)
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

        var lista  = notaDAO.listarPorRango(desde, hasta);
        var diario = notaDAO.resumenDiario(desde, hasta);
        var reps   = notaDAO.resumenPorRepartidor(desde, hasta);

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
     * helpers JSON & parser
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

    /* ---------------------------------------------------------------
     * 10. Imprime TODAS las notas del día para un repartidor (iText)
     *     URL: …/NotaVentaServlet?accion=imprimirNotasDia&id_repartidor=##
     * ------------------------------------------------------------- */
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
                    .setBold()
                    .setMargin(0);
            Table cab = new Table(new float[]{1, 3})
                    .useAllAvailableWidth()
                    .setBorder(Border.NO_BORDER);
            cab.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
            cab.addCell(new Cell().add(datosEmp).setBorder(Border.NO_BORDER));
            doc.add(cab).add(new Paragraph("\n"));

            double granTotal = 0;
            for (NotaVenta nota : notas) {
                doc.add(new Paragraph("NOTA DE VENTA  #" + nota.getFolio())
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(12)
                        .setBold());

                doc.add(new Paragraph()
                        .add("Tienda: ")
                        .add(tiendaDAO.buscarPorId(nota.getIdTienda()).getNombre())
                        .add("\nFecha: " + nota.getFechaNota())
                        .setMarginBottom(6));

                List<DetalleNotaVenta> det =
                        detalleDAO.listarPorNota(nota.getIdNotaVenta());
                for (DetalleNotaVenta d : det) {
                    d.setNombreEmpaque(
                            empaqueDAO.buscarPorId(d.getIdEmpaque()).getNombreEmpaque());
                }

                Table tbl = new Table(new float[]{5,2,2,2}).useAllAvailableWidth();
                tbl.addHeaderCell(header("Empaque"));
                tbl.addHeaderCell(header("Vendidos"));
                tbl.addHeaderCell(header("Merma"));
                tbl.addHeaderCell(header("Subtotal"));

                double total = 0;
                for (DetalleNotaVenta d : det) {
                    tbl.addCell(cell(d.getNombreEmpaque()));
                    tbl.addCell(cell(d.getCantidadVendida()));
                    tbl.addCell(cell(d.getMerma()));
                    tbl.addCell(cell(String.format("$ %.2f", d.getTotalLinea())));
                    total += d.getTotalLinea();
                }
                doc.add(tbl);
                doc.add(new Paragraph("Total nota: $ " + String.format("%.2f", total))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBold()
                        .setMarginBottom(10));
                granTotal += total;

                if (nota != notas.get(notas.size()-1)) {
                    doc.add(new Paragraph("\n").setBorderTop(new SolidBorder(0.5f)));
                }
            }

            doc.add(new Paragraph("TOTAL DEL DÍA: $ " + String.format("%.2f", granTotal))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(13)
                    .setBold()
                    .setMarginTop(15));
            doc.close();
        } catch (Exception e) {
            throw new ServletException("No se pudo generar el PDF", e);
        }
    }

    /* Helpers abreviados */
    private Cell header(String txt){
        return new Cell().add(new Paragraph(txt))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold();
    }
    private Cell cell(Object txt){
        return new Cell().add(new Paragraph(String.valueOf(txt)));
    }

    private List<DetalleNotaVenta> parseDetalleJSON(String json,int idNota){
        List<DetalleNotaVenta> list = new ArrayList<>();
        if (json == null || json.isBlank()) return list;
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement el : arr){
            JsonObject o = el.getAsJsonObject();
            DetalleNotaVenta d = new DetalleNotaVenta();
            d.setIdNota            (idNota);
            d.setIdEmpaque         (safeInt(o,"idEmpaque"));
            d.setIdDistribucion    (safeInt(o,"idDistribucion"));
            d.setCantidadVendida   (safeInt(o,"vendidas"));
            d.setMerma             (safeInt(o,"merma"));
            d.setPrecioUnitario    (safeDouble(o,"precio"));
            list.add(d);
        }
        return list;
    }
    private int    safeInt   (JsonObject o,String k){ return o.has(k)&&!o.get(k).isJsonNull()?o.get(k).getAsInt():0; }
    private double safeDouble(JsonObject o,String k){ return o.has(k)&&!o.get(k).isJsonNull()?o.get(k).getAsDouble():0.0; }
}
