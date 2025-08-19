package servlet;

import dao.DistribucionDAO;
import dto.DistribucionFilaDTO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador del módulo “Salidas de Distribución”.
 * – Soporta AJAX (fetch) y navegación clásica.
 * – Devuelve JSON plano cuando aplica.
 */
@WebServlet("/DistribucionServlet")
public class DistribucionServlet extends HttpServlet {

    private final DistribucionDAO dao = new DistribucionDAO();

    /* ---- Clave de sesión local a este módulo ---- */
    private static final String FLASH_SALIDA = "flashSalida";

    /* -------------------------------- util ----------------------------- */
    private static boolean esAjax(HttpServletRequest req) {
        String hdr = req.getHeader("X-Requested-With");
        return "fetch".equalsIgnoreCase(hdr) || "XMLHttpRequest".equalsIgnoreCase(hdr);
    }

    /** ¿Está este servlet siendo invocado mediante <jsp:include>? */
    private static boolean esInclude(HttpServletRequest req) {
        return req.getAttribute("javax.servlet.include.request_uri") != null
            || req.getAttribute("javax.servlet.include.servlet_path") != null;
    }

    private static LocalDateTime parseDateTimeLenient(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String t = s.trim();
        if (t.contains(" ") && !t.contains("T")) {
            t = t.replace(' ', 'T');
        }
        return LocalDateTime.parse(t);
    }

    /* ------------------------------ GET -------------------------------- */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");
        String ctx    = req.getContextPath();

        try {
            switch (accion == null ? "" : accion) {

                /* ------ Listar (carga datos para el JSP) ------ */
                case "listar" -> {
                    LocalDate fecha = (req.getParameter("fecha") == null ||
                                       req.getParameter("fecha").isBlank())
                                      ? LocalDate.now()
                                      : LocalDate.parse(req.getParameter("fecha"));

                    var salidas = dao.listarPorFecha(fecha);
                    req.setAttribute("salidas", salidas);

                    // Si nos llamaron con <jsp:include>, solo setAttribute y salimos.
                    if (esInclude(req)) {
                        return;
                    }
                    // Si abren directamente el servlet, renderiza el JSP:
                    req.getRequestDispatcher("/jsp/distribucion/distribucionList.jsp")
                       .forward(req, resp);
                    return;
                }

                /* ------ Detalle JSON (para modal) ------ */
                case "detalle" -> {
                    int idRep = Integer.parseInt(req.getParameter("idRepartidor"));
                    LocalDateTime f = parseDateTimeLenient(req.getParameter("fecha"));
                    if (f == null) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fecha inválida");
                        return;
                    }
                    List<DistribucionFilaDTO> filas =
                            dao.buscarPorRepartidorYFechaHora(idRep, f);

                    resp.setContentType("application/json;charset=UTF-8");
                    StringBuilder json = new StringBuilder("[");
                    for (DistribucionFilaDTO d : filas) {
                        json.append("{\"empaque\":\"")
                            .append(escape(d.getNombreEmpaque()))
                            .append("\",\"cantidad\":")
                            .append(d.getCantidad())
                            .append("},");
                    }
                    if (json.length() > 1) json.setLength(json.length() - 1);
                    json.append(']');
                    resp.getWriter().print(json.toString());
                    return;
                }

                /* ------ Eliminar TODA una salida (por repartidor+fechaHora) ------ */
                case "delete" -> {
                    int idRep = Integer.parseInt(req.getParameter("idRepartidor"));
                    LocalDateTime f = parseDateTimeLenient(req.getParameter("fecha"));
                    if (f == null) {
                        req.getSession().setAttribute(FLASH_SALIDA, "Fecha inválida.");
                    } else {
                        dao.eliminarSalida(idRep, f);
                        req.getSession().setAttribute(FLASH_SALIDA,
                                "Salida eliminada; inventario restituido.");
                    }
                    resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
                    return;
                }

                default -> {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no soportada");
                    return;
                }
            }

        } catch (Exception ex) {
            // Mensaje de error local a este módulo
            req.getSession().setAttribute(FLASH_SALIDA, "Error: " + ex.getMessage());
            resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
        }
    }

    /* ------------------------------ POST ------------------------------- */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");
        boolean ajax  = esAjax(req);
        String  ctx   = req.getContextPath();

        try {
            switch (accion == null ? "" : accion) {

                /* ------ Crear salida (desde modal) ----------- */
                case "crear" -> {
                    int idRepartidor = Integer.parseInt(req.getParameter("idRepartidor"));

                    String[] idEmpaques = req.getParameterValues("id_empaque[]");
                    String[] cantidades = req.getParameterValues("cantidad[]");
                    if (idEmpaques == null || cantidades == null || idEmpaques.length != cantidades.length) {
                        if (ajax) {
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            resp.getWriter().print("Parámetros inválidos");
                            return;
                        } else {
                            req.getSession().setAttribute(FLASH_SALIDA, "Parámetros inválidos");
                            resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
                            return;
                        }
                    }

                    Map<Integer, Integer> lineas = new LinkedHashMap<>();
                    for (int i = 0; i < idEmpaques.length; i++) {
                        int idEmp = Integer.parseInt(idEmpaques[i]);
                        int cant  = Integer.parseInt(cantidades[i]);
                        if (cant > 0) lineas.put(idEmp, cant);
                    }

                    if (lineas.isEmpty()) {
                        if (ajax) {
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            resp.getWriter().print("No hay líneas válidas.");
                            return;
                        } else {
                            req.getSession().setAttribute(FLASH_SALIDA, "No hay líneas válidas.");
                            resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
                            return;
                        }
                    }

                    // >>> Un solo punto de descuento en bodega (dentro del DAO)
                    dao.crearSalida(idRepartidor, lineas);

                    req.getSession().setAttribute(FLASH_SALIDA, "Salida registrada correctamente.");

                    if (ajax) {
                        resp.setContentType("text/plain;charset=UTF-8");
                        resp.getWriter().print("OK");
                        return; // no redirigir en AJAX
                    } else {
                        resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
                        return;
                    }
                }

                /* ------ Editar línea (inline, spinner del input) ----------- */
                case "editarLinea" -> {
                    int idDist = Integer.parseInt(req.getParameter("idDistribucion"));
                    int nueva  = Integer.parseInt(req.getParameter("cantidad"));

                    // >>> Ajuste integral (mueve bodega y rep dentro del DAO)
                    dao.actualizarCantidad(idDist, nueva);

                    if (ajax) {
                        resp.setContentType("text/plain;charset=UTF-8");
                        resp.getWriter().print("OK");
                        return;
                    } else {
                        req.getSession().setAttribute(FLASH_SALIDA, "Cantidad actualizada.");
                        resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
                        return;
                    }
                }

                default -> {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no soportada");
                    return;
                }
            }

        } catch (Exception ex) {
            if (ajax) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("text/plain;charset=UTF-8");
                resp.getWriter().print(ex.getMessage());
            } else {
                req.getSession().setAttribute(FLASH_SALIDA, "Error: " + ex.getMessage());
                resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
            }
        }
    }

    /* ------------------------------ helper ----------------------------- */
    /** Escapa comillas y backslash para JSON simple. */
    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
