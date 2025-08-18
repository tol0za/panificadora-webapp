package servlet;

import dao.DistribucionDAO;
import dto.DistribucionFilaDTO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del módulo “Salidas de Distribución”.
 * – Soporta AJAX (fetch) y navegación clásica.
 * – Devuelve JSON sin usar javax.json para máxima compatibilidad.
 */
@WebServlet("/DistribucionServlet")
public class DistribucionServlet extends HttpServlet {

    private final DistribucionDAO dao = new DistribucionDAO();

    /* ---- Claves de sesión para evitar fugas entre módulos ---- */
    private static final String FLASH_SALIDA = "flashSalida";

    /* -------------------------------- util ----------------------------- */
    private static boolean esAjax(HttpServletRequest req) {
        String hdr = req.getHeader("X-Requested-With");
        return "fetch".equalsIgnoreCase(hdr) || "XMLHttpRequest".equalsIgnoreCase(hdr);
    }

    /* ------------------------------ GET -------------------------------- */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");
        String ctx    = req.getContextPath();

        try {
            switch (accion == null ? "" : accion) {

                /* ------ Listar (carga JSP) ------ */
                case "listar" -> {
                    LocalDate fecha = (req.getParameter("fecha") == null ||
                                       req.getParameter("fecha").isBlank())
                                      ? LocalDate.now()
                                      : LocalDate.parse(req.getParameter("fecha"));
                    List<?> salidas = dao.listarPorFecha(fecha);
                    req.setAttribute("salidas", salidas);
                    req.getRequestDispatcher("/jsp/distribucion/distribucionList.jsp")
                       .forward(req, resp);
                    return;
                }

                /* ------ Detalle JSON ------------ */
                case "detalle" -> {
                    int idRep = Integer.parseInt(req.getParameter("idRepartidor"));
                    LocalDateTime f = LocalDateTime.parse(req.getParameter("fecha"));
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
                    if (json.length() > 1) json.setLength(json.length() - 1); // quita coma final
                    json.append(']');
                    resp.getWriter().print(json.toString());
                    return; // evita redirección
                }

                /* ------ Eliminar salida --------- */
                case "delete" -> {
                    int idRep = Integer.parseInt(req.getParameter("idRepartidor"));
                    LocalDateTime f = LocalDateTime.parse(req.getParameter("fecha"));
                    dao.eliminarSalida(idRep, f);
                    // usar clave local para que solo aparezca en distribucionList.jsp
                    req.getSession().setAttribute(FLASH_SALIDA,
                            "Salida eliminada; inventario restituido.");
                }

                default -> {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no soportada");
                    return;
                }
            }
        } catch (Exception ex) {
            // Mensaje de error local a este módulo
            req.getSession().setAttribute(FLASH_SALIDA, "Error: " + ex.getMessage());
        }

        // Solo delete cae aquí: navegar al JSP (el JSP hará include de “listar” si lo necesita)
        resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
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
                    int idRep = Integer.parseInt(req.getParameter("idRepartidor"));

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
                        if (cant > 0) {
                            lineas.put(idEmp, cant);
                        }
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

                    dao.crearSalida(idRep, lineas);
                    // CLAVE: usar flashSalida para que solo lo lea distribucionList.jsp
                    req.getSession().setAttribute(FLASH_SALIDA, "Salida registrada correctamente.");

                    // Respuesta según tipo
                    if (ajax) {
                        resp.setContentType("text/plain;charset=UTF-8");
                        resp.getWriter().print("OK");
                        return; // no redirigir en AJAX
                    } else {
                        resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
                        return;
                    }
                }

                /* ------ Editar línea (inline) ----------- */
                case "editarLinea" -> {
                    int idDist = Integer.parseInt(req.getParameter("idDistribucion"));
                    int nueva  = Integer.parseInt(req.getParameter("cantidad"));
                    dao.actualizarCantidad(idDist, nueva);
                    req.getSession().setAttribute(FLASH_SALIDA, "Cantidad actualizada.");

                    if (ajax) {
                        resp.setContentType("text/plain;charset=UTF-8");
                        resp.getWriter().print("OK");
                        return;
                    } else {
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
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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