package servlet;

import dao.CatalogoEmpaqueDAO;
import dao.InventarioEmpaquetadoDAO;
import modelo.CatalogoEmpaque;
import modelo.InventarioEmpaquetado;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.Locale;

/**
 * Módulo de Inventario (bodega general).
 * - Navegación clásica + embebido en iframe (inicio.jsp).
 * - Endpoint JSON: ?accion=movsByEmpaque&idEmpaque=##
 */
public class InventarioServlet extends HttpServlet {

    /* ---------------- DAOs ---------------- */
    private InventarioEmpaquetadoDAO inventarioDAO;
    private CatalogoEmpaqueDAO       empaqueDAO;

    /* ------------- claves de flash por módulo ------------- */
    private static final String FLASH_INV    = "flashInv";     // ← solo inventario
    private static final String FLASH_SALIDA = "flashSalida";  // otros módulos
    private static final String FLASH_NOTAS  = "flashNotas";   // otros módulos

    /* ================== init ================== */
    @Override
    public void init() throws ServletException {
        try {
            inventarioDAO = new InventarioEmpaquetadoDAO();
            empaqueDAO    = new CatalogoEmpaqueDAO();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /* ================ helpers ================ */

    /** ¿Viene embebido desde inicio.jsp? (evita redirecciones dobles) */
    private boolean fromIframe(HttpServletRequest req) {
        if ("1".equals(req.getParameter("inFrame"))) return true;
        String ref = req.getHeader("referer");
        return ref != null && ref.contains("/jsp/home/inicio.jsp");
    }

    /** ¿La petición es AJAX y explícitamente espera JSON? */
    private boolean isAjaxJson(HttpServletRequest req) {
        String xr  = req.getHeader("X-Requested-With");
        String acc = req.getHeader("Accept");
        boolean ajax = xr != null && (xr.equalsIgnoreCase("XMLHttpRequest") || xr.equalsIgnoreCase("fetch"));
        boolean wantsJson = acc != null && acc.toLowerCase(Locale.ROOT).contains("application/json");
        return ajax && wantsJson;
    }

    private String firstNonBlank(String... ss) {
        for (String s : ss) if (s != null && !s.isBlank()) return s;
        return null;
    }
private static String formatFecha(Object f) {
    if (f == null) return "";
    try {
        if (f instanceof java.time.temporal.TemporalAccessor ta) {
            return java.time.format.DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss")
                    .format(ta);
        }
    } catch (Throwable ignore) { /* seguimos con otros casos */ }

    if (f instanceof Timestamp ts) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);
    }
    if (f instanceof java.util.Date d) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
    }
    return String.valueOf(f); // último recurso
}
    /**
     * Lee idEmpaque desde:
     *  - parámetros: idEmpaque / id_empaque / id
     *  - header     : X-IdEmpaque (respaldo si un filtro limpia la query)
     *  - queryString crudo (último recurso)
     */
    private String readIdEmpaque(HttpServletRequest req) {
        String id = firstNonBlank(
                req.getParameter("idEmpaque"),
                req.getParameter("id_empaque"),
                req.getParameter("id"),
                req.getHeader("X-IdEmpaque")
        );
        if ((id == null || id.isBlank()) && req.getQueryString() != null) {
            String qs = req.getQueryString();
            for (String kv : qs.split("&")) {
                int p = kv.indexOf('=');
                if (p <= 0) continue;
                String k = kv.substring(0, p), v = kv.substring(p + 1);
                if ("idEmpaque".equals(k) || "id_empaque".equals(k) || "id".equals(k)) { id = v; break; }
            }
        }
        return id;
    }

    /** Carga empaques, mapa de stock y (si se pide) movimientos para la vista. */
    private void cargarAtributos(HttpServletRequest req) throws SQLException {
        List<CatalogoEmpaque> empaques = empaqueDAO.findAll();
        Map<Integer, Integer> stockMap  = new HashMap<>();
        for (CatalogoEmpaque c : empaques) {
            int stock = inventarioDAO.obtenerCantidadActual(c.getIdEmpaque());
            stockMap.put(c.getIdEmpaque(), stock);
        }
        req.setAttribute("empaques", empaques);
        req.setAttribute("stockMap", stockMap);
        // La tabla grande ya no es necesaria para el modal por empaque,
        // pero si la JSP sigue mostrándola, la dejamos:
        req.setAttribute("movimientos", inventarioDAO.listarMovimientos());
    }

    /** JSON-safe para motivos. */
    private static String esc(String s) {
        return (s == null) ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Emite JSON de movimientos por empaque. Lee y valida el ID internamente. */
   
private void sendMovsJson(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.setCharacterEncoding("UTF-8");
    res.setContentType("application/json;charset=UTF-8");
    res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");

    String idStr = readIdEmpaque(req);
    if (idStr == null || idStr.isBlank()) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().print("{\"error\":\"idEmpaque no recibido\"}");
        return;
    }

    final int idEmpaque;
    try {
        idEmpaque = Integer.parseInt(idStr.trim());
        if (idEmpaque <= 0) throw new NumberFormatException();
    } catch (NumberFormatException nfe) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().print("{\"error\":\"idEmpaque no es un número válido\"}");
        return;
    }

    try {
        // 1) Intento directo por empaque (lo ideal)
        List<InventarioEmpaquetado> movs = inventarioDAO.listarMovimientosPorEmpaque(idEmpaque);

        // 2) Fallback: si vino vacío, filtra en memoria el listar general
        if (movs == null || movs.isEmpty()) {
            List<InventarioEmpaquetado> todos = inventarioDAO.listarMovimientos();
            if (todos != null) {
                movs = new ArrayList<>();
                for (InventarioEmpaquetado m : todos) {
                    // OJO: el modelo expone getIdEmpaque() en camelCase,
                    //      aunque la columna en BD es id_empaque. :contentReference[oaicite:1]{index=1}
                    if (m.getIdEmpaque() == idEmpaque) movs.add(m);
                }
            } else {
                movs = Collections.emptyList();
            }
        }

        // 3) Arma JSON
        StringBuilder sb = new StringBuilder("[");
        for (InventarioEmpaquetado m : movs) {
            sb.append("{\"fecha\":\"").append(formatFecha(m.getFecha()))
              .append("\",\"motivo\":\"").append(esc(m.getMotivo()))
              .append("\",\"cantidad\":").append(m.getCantidad())
              .append(",\"cantidadActual\":").append(m.getCantidadActual())
              .append("},");
        }
        if (sb.length() > 1) sb.setLength(sb.length() - 1);
        sb.append("]");

        res.getWriter().print(sb.toString());
    } catch (Exception ex) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String msg = ex.getMessage()==null ? ex.toString() : ex.getMessage();
        res.getWriter().print("{\"error\":\"Error interno: " + msg.replace("\"","\\\"") + "\"}");
    }
}
    /* ================== GET ================== */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // BYPASS UNIVERSAL PARA AJAX JSON:
        // si es AJAX+JSON y trae ID (parámetro o header), respondemos JSON y salimos.
        if (isAjaxJson(req) && readIdEmpaque(req) != null) {
            sendMovsJson(req, res);
            return;
        }

        String acc = req.getParameter("accion");
        if (acc == null) acc = "listar";

        // Al entrar al módulo Inventario, limpia flashes *ajenos*.
        HttpSession ses = req.getSession(false);
        if (ses != null) {
            ses.removeAttribute(FLASH_SALIDA);
            ses.removeAttribute(FLASH_NOTAS);
            // compatibilidad con mensajes antiguos:
            ses.removeAttribute("flashMsg");
            ses.removeAttribute("mensaje");
        }

        try {
            switch (acc) {

                /* ---------- JSON: movimientos por empaque ---------- */
                case "movsByEmpaque" -> {
                    sendMovsJson(req, res);
                    return;
                }

                /* ---------- Vista de inventario ---------- */
                case "listar" -> {
                    cargarAtributos(req);

                    // Si NO viene del iframe: redirige a inicio.jsp con la vista.
                    if (!fromIframe(req)) {
                        res.sendRedirect(req.getContextPath()
                                + "/jsp/home/inicio.jsp?vista=jsp/inventario/inventarioList.jsp");
                        return;
                    }

                    // Si viene del iframe: forward directo al JSP.
                    req.getRequestDispatcher("/jsp/inventario/inventarioList.jsp")
                       .forward(req, res);
                    return;
                }

                /* ---------- Cualquier otra: manda a listar en iframe ---------- */
                default -> {
                    res.sendRedirect(req.getContextPath()
                            + "/InventarioServlet?inFrame=1&accion=listar");
                    return;
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /* ================== POST ================== */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        try {
            int    idEmpaque = Integer.parseInt(req.getParameter("idEmpaque"));
            int    cantidad  = Integer.parseInt(req.getParameter("cantidad"));
            String motivo    = Optional.ofNullable(req.getParameter("motivo")).orElse("").trim();

            // Ajuste de signo para salidas/mermas
            String mLC = motivo.toLowerCase();
            if (mLC.contains("salida") || mLC.contains("merma")) {
                cantidad = -Math.abs(cantidad);
            } else {
                cantidad =  Math.abs(cantidad);
            }

            int cantidadActual = inventarioDAO.obtenerCantidadActual(idEmpaque);
            int nuevaCantidad  = cantidadActual + cantidad;
            if (nuevaCantidad < 0) {
                req.getSession().setAttribute(FLASH_INV,
                        "No hay suficiente inventario para esta operación.");
                res.sendRedirect(req.getContextPath()
                        + "/InventarioServlet?inFrame=1&accion=listar");
                return;
            }

            // Registrar movimiento
            InventarioEmpaquetado mov = new InventarioEmpaquetado();
            mov.setIdEmpaque(idEmpaque);
            mov.setCantidad(cantidad);
            mov.setFecha(LocalDateTime.now());
            mov.setMotivo(motivo);
            mov.setCantidadActual(nuevaCantidad);
            inventarioDAO.registrarMovimiento(mov);

            // Flash SOLO para inventario
            String signo   = (cantidad >= 0 ? "+" : "");
            String mensaje = "<strong>Stock anterior:</strong> " + cantidadActual + " unidades<br>"
                    + "<strong>Movimiento:</strong> " + signo + cantidad + " unidades<br>"
                    + "<strong>Stock resultante:</strong> " + nuevaCantidad + " unidades";
            req.getSession().setAttribute(FLASH_INV, mensaje);

        } catch (Exception e) {
            req.getSession().setAttribute(FLASH_INV,
                    "Error al registrar movimiento: " + e.getMessage());
        }

        // Siempre regresa a la vista embebida
        res.sendRedirect(req.getContextPath()
                + "/InventarioServlet?inFrame=1&accion=listar");
    }
}
