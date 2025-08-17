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
                    List<DistribucionFilaDTO> filas = dao.buscarPorRepartidorYFechaHora(idRep, f);

                    resp.setContentType("application/json;charset=UTF-8");
                    StringBuilder json = new StringBuilder("[");
                    for (DistribucionFilaDTO d : filas) {
                        json.append("{\"empaque\":\"")
                            .append(escape(d.getNombreEmpaque()))
                            .append("\",\"cantidad\":")
                            .append(d.getCantidad())
                            .append("},");
                    }
                    if (json.length() > 1) json.setLength(json.length()-1); // quita coma final
                    json.append(']');
                    resp.getWriter().print(json.toString());
                    return;                         // evita redirección
                }

                /* ------ Eliminar salida --------- */
                case "delete" -> {
                    int idRep = Integer.parseInt(req.getParameter("idRepartidor"));
                    LocalDateTime f = LocalDateTime.parse(req.getParameter("fecha"));
                    dao.eliminarSalida(idRep, f);
                    req.getSession().setAttribute("mensaje",
                            "Salida eliminada; inventario restituido.");
                }

                default -> {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no soportada");
                    return;
                }
            }

        } catch (Exception ex) {
            req.getSession().setAttribute("error", ex.getMessage());
        }

        /* Solo delete redirige (navegación clásica) */
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

                /* ------ Crear salida ----------- */
                case "crear" -> {
                    int idRep = Integer.parseInt(req.getParameter("idRepartidor"));
                    String[] idEmpaques = req.getParameterValues("id_empaque[]");
                    String[] cantidades = req.getParameterValues("cantidad[]");

                    Map<Integer,Integer> lineas = new LinkedHashMap<>();
                    for (int i = 0; i < idEmpaques.length; i++) {
                        lineas.put(Integer.parseInt(idEmpaques[i]),
                                   Integer.parseInt(cantidades[i]));
                    }
                    dao.crearSalida(idRep, lineas);
                    req.getSession().setAttribute("mensaje", "Salida registrada.");
                }

                /* ------ Editar línea ----------- */
                case "editarLinea" -> {
                    int idDist = Integer.parseInt(req.getParameter("idDistribucion"));
                    int nueva  = Integer.parseInt(req.getParameter("cantidad"));
                    dao.actualizarCantidad(idDist, nueva);
                    req.getSession().setAttribute("mensaje", "Cantidad actualizada.");
                }

                default -> {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no soportada");
                    return;
                }
            }

            /* ------ Respuesta --------- */
            if (ajax) {
                resp.setContentType("text/plain");
                resp.getWriter().print("OK");
            } else {
                resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
            }

        } catch (Exception ex) {
            if (ajax) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print(ex.getMessage());
            } else {
                req.getSession().setAttribute("error", ex.getMessage());
                resp.sendRedirect(ctx + "/jsp/distribucion/distribucionList.jsp");
            }
        }
    }

    /* ------------------------------ helper ----------------------------- */
    /** Escapa comillas y backslash para JSON simple. */
    private static String escape(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }
}