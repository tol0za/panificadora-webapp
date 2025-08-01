package servlet;
import dao.SalidaDAO;
import dao.RepartidorDAO;
import dao.CatalogoEmpaqueDAO;
import dao.InventarioEmpaquetadoDAO;
import modelo.Salida;
import modelo.Repartidor;
import modelo.CatalogoEmpaque;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class SalidaServlet extends HttpServlet {
    private SalidaDAO salidaDAO;
    private RepartidorDAO repartidorDAO;
    private CatalogoEmpaqueDAO empaqueDAO;
    private InventarioEmpaquetadoDAO inventarioDAO;

    @Override
    public void init() throws ServletException {
        try {
            salidaDAO = new SalidaDAO();
            repartidorDAO = new RepartidorDAO();
            empaqueDAO = new CatalogoEmpaqueDAO();
            inventarioDAO = new InventarioEmpaquetadoDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al inicializar DAOs", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        // NUEVA ACCIÓN PARA CARGAR EMPAQUES EN FORMATO JSON
       if ("obtenerEmpaques".equals(accion)) {
    try {
        cargarEmpaques(response);
    } catch (SQLException e) {
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Error al obtener empaques: " + e.getMessage() + "\"}");
    }
    return;
}


        try {
            if (accion == null || accion.isEmpty() || "calendario".equals(accion)) {
                List<Salida> salidas = salidaDAO.listarSalidas();
                request.setAttribute("salidas", salidas);
                request.getRequestDispatcher("/jsp/salidas/salidaCalendario.jsp").forward(request, response);
            } else if ("listar".equals(accion)) {
                List<Salida> salidas = salidaDAO.listarSalidas();
                request.setAttribute("salidas", salidas);
                request.getRequestDispatcher("/jsp/salidas/salidaList.jsp").forward(request, response);
            } else if ("nuevo".equals(accion)) {
                List<Repartidor> repartidores = repartidorDAO.listar();
                List<CatalogoEmpaque> empaques = empaqueDAO.findAll();
                Map<Integer, Integer> stockMap = new HashMap<>();
                for (CatalogoEmpaque em : empaques) {
                    stockMap.put(em.getIdEmpaque(), inventarioDAO.obtenerCantidadActual(em.getIdEmpaque()));
                }
                List<CatalogoEmpaque> empaquesConStock = new ArrayList<>();
                Map<Integer, Integer> stockFiltrado = new HashMap<>();
                for (CatalogoEmpaque e : empaques) {
                    int stock = stockMap.getOrDefault(e.getIdEmpaque(), 0);
                    if (stock > 0) {
                        empaquesConStock.add(e);
                        stockFiltrado.put(e.getIdEmpaque(), stock);
                    }
                }
                request.setAttribute("repartidores", repartidores);
                request.setAttribute("empaques", empaquesConStock);
                request.setAttribute("stockMap", stockFiltrado);
                request.getRequestDispatcher("/jsp/salidas/salidaForm.jsp").forward(request, response);
            } else if ("editar".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Salida salida = salidaDAO.buscarPorId(id);
                if (salida == null) {
                    request.getSession().setAttribute("mensaje", "Salida no encontrada.");
                    response.sendRedirect("SalidaServlet");
                    return;
                }
                List<Repartidor> repartidores = repartidorDAO.listar();
                List<CatalogoEmpaque> empaques = empaqueDAO.findAll();
                request.setAttribute("repartidores", repartidores);
                request.setAttribute("empaques", empaques);
                request.setAttribute("salida", salida);
                request.getRequestDispatcher("/jsp/salidas/salidaEditar.jsp").forward(request, response);
            } else if ("editarMultiple".equals(accion)) {
                int idR = Integer.parseInt(request.getParameter("idRepartidor"));
                String fecha = request.getParameter("fecha");
                List<Salida> detalles = salidaDAO.listarSalidasPorRepartidorYFecha(idR, fecha);
                String nombreR = detalles.isEmpty() ? ""
                        : detalles.get(0).getNombreRepartidor() + " " + detalles.get(0).getApellidoRepartidor();
                request.setAttribute("detalles", detalles);
                request.setAttribute("idRepartidor", idR);
                request.setAttribute("fechaSeleccionada", fecha);
                request.setAttribute("nombreRepartidor", nombreR);
                request.getRequestDispatcher("/jsp/salidas/salidaEditarMultiple.jsp").forward(request, response);
                return;
            } else if ("eliminarSalida".equals(accion)) {
                String strIdR = request.getParameter("idRepartidor");
                String fecha  = request.getParameter("fecha");
                if (strIdR == null || fecha == null || strIdR.isEmpty() || fecha.isEmpty()) {
                    request.getSession().setAttribute("mensaje", "Parámetros inválidos para eliminar la salida.");
                    response.sendRedirect("SalidaServlet?accion=verDia&fecha=" + (fecha != null ? fecha : ""));
                    return;
                }
                salidaDAO.eliminarSalidaPorRepartidorYFecha(Integer.parseInt(strIdR), fecha);
                request.getSession().setAttribute("mensaje", "Salida eliminada correctamente.");
                response.sendRedirect("SalidaServlet?accion=verDia&fecha=" + fecha);
                return;
            } else if ("verDia".equals(accion)) {
                String fecha = request.getParameter("fecha");
                List<Salida> salidasDia = salidaDAO.listarSalidasPorFecha(fecha);
                Map<Integer, List<Salida>> porRep = new LinkedHashMap<>();
                for (Salida s : salidasDia) {
                    porRep.computeIfAbsent(s.getIdRepartidor(), k -> new ArrayList<>()).add(s);
                }
                request.setAttribute("salidasPorRepartidor", porRep);
                request.setAttribute("fechaSeleccionada", fecha);
                request.getRequestDispatcher("/jsp/salidas/salidasDia.jsp").forward(request, response);
            } else if ("verDetalle".equals(accion)) {
                int idR = Integer.parseInt(request.getParameter("idRepartidor"));
                String fecha = request.getParameter("fecha");
                List<Salida> detalles = salidaDAO.listarSalidasPorRepartidorYFecha(idR, fecha);
                String nombreR = detalles.isEmpty() ? ""
                        : detalles.get(0).getNombreRepartidor() + " " + detalles.get(0).getApellidoRepartidor();
                request.setAttribute("detalles", detalles);
                request.setAttribute("nombreRepartidor", nombreR);
                request.setAttribute("fechaSeleccionada", fecha);
                request.getRequestDispatcher("/jsp/salidas/salidasPorRepartidor.jsp").forward(request, response);
            } else {
                List<Salida> salidas = salidaDAO.listarSalidas();
                request.setAttribute("salidas", salidas);
                request.getRequestDispatcher("/jsp/salidas/salidaList.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("mensajeError", "Error: " + e.getMessage());
            request.getRequestDispatcher("/jsp/salidas/salidaList.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if ("eliminarArticulo".equals(accion)) {
            String strId  = request.getParameter("idDistribucion");
            String strIdR = request.getParameter("idRepartidor");
            String fecha  = request.getParameter("fecha");
            if (strId == null || strId.isEmpty()
             || strIdR == null || strIdR.isEmpty()
             || fecha == null || fecha.isEmpty()) {
                request.getSession().setAttribute("mensaje", "Parámetros inválidos para eliminar el artículo.");
                response.sendRedirect("SalidaServlet?accion=editarMultiple&idRepartidor=" + strIdR + "&fecha=" + fecha);
                return;
            }
            try {
                salidaDAO.eliminarSalida(Integer.parseInt(strId));
                request.getSession().setAttribute("mensaje", "Artículo eliminado correctamente.");
            } catch (SQLException ex) {
                request.getSession().setAttribute("mensaje", "Error al eliminar artículo: " + ex.getMessage());
            }
            response.sendRedirect("SalidaServlet?accion=editarMultiple&idRepartidor=" + strIdR + "&fecha=" + fecha);
            return;
        }
        try {
            if ("actualizar".equals(accion)) {
                int idDist = Integer.parseInt(request.getParameter("idDistribucion"));
                int cant   = Integer.parseInt(request.getParameter("cantidad"));
                Salida s   = salidaDAO.buscarPorId(idDist);
                if (s != null) {
                    s.setCantidad(cant);
                    salidaDAO.actualizarSalida(s);
                }
                request.getSession().setAttribute("mensaje", "Salida actualizada correctamente.");
                response.sendRedirect("SalidaServlet");
                return;
            } else if ("actualizarMultiple".equals(accion)) {
                String[] ids   = request.getParameterValues("idDistribucion[]");
                String[] cants = request.getParameterValues("cantidad[]");
                for (int i = 0; i < ids.length; i++) {
                    Salida s = salidaDAO.buscarPorId(Integer.parseInt(ids[i]));
                    if (s != null) {
                        s.setCantidad(Integer.parseInt(cants[i]));
                        salidaDAO.actualizarSalida(s);
                    }
                }
                String fecha = request.getParameter("fecha");
                int idR      = Integer.parseInt(request.getParameter("idRepartidor"));
                request.getSession().setAttribute("mensaje", "Salida actualizada correctamente.");
                response.sendRedirect("SalidaServlet?accion=editarMultiple&idRepartidor=" + idR + "&fecha=" + fecha);
                return;
            } else {
                int idRepartidor   = Integer.parseInt(request.getParameter("idRepartidor"));
                String[] empaques  = request.getParameterValues("idEmpaque[]");
                String[] cantidades= request.getParameterValues("cantidad[]");
                if (empaques == null || cantidades == null || empaques.length != cantidades.length) {
                    request.getSession().setAttribute("mensaje", "Error: Debes seleccionar al menos un producto y cantidad.");
                    response.sendRedirect("SalidaServlet?accion=nuevo");
                    return;
                }
                for (int i = 0; i < empaques.length; i++) {
                    int idE = Integer.parseInt(empaques[i]);
                    int c   = Integer.parseInt(cantidades[i]);
                    if (c <= 0) continue;
                    Salida s = new Salida();
                    s.setIdRepartidor(idRepartidor);
                    s.setIdEmpaque(idE);
                    s.setCantidad(c);
                    s.setFechaDistribucion(LocalDateTime.now());
                    salidaDAO.registrarSalida(s);
                }
                request.getSession().setAttribute("mensaje", "Salida registrada correctamente.");
                response.sendRedirect("SalidaServlet?accion=nuevo");
                return;
            }
        } catch (Exception e) {
            request.getSession().setAttribute("mensaje", "Error al procesar: " + e.getMessage());
            response.sendRedirect("SalidaServlet");
        }
    }

    // NUEVO MÉTODO PARA RETORNAR EMPAQUES EN JSON
private void cargarEmpaques(HttpServletResponse response) throws IOException, SQLException {
        response.setContentType("application/json");
        List<CatalogoEmpaque> empaques = empaqueDAO.findAll();
        PrintWriter out = response.getWriter();
        out.println("[");
        for (int i = 0; i < empaques.size(); i++) {
            CatalogoEmpaque emp = empaques.get(i);
            out.print("{");
            out.printf("\"id\": %d, \"nombre\": \"%s\"", emp.getIdEmpaque(), emp.getNombreEmpaque());
            out.print("}");
            if (i < empaques.size() - 1) {
                out.print(",");
            }
        }
        out.println("]");
    }
}