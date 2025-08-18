package servlet;

import dao.SalidaDAO;
import dao.RepartidorDAO;
import dao.CatalogoEmpaqueDAO;
import dao.InventarioEmpaquetadoDAO;
import dao.InventarioRepartidorDAO;        // ← NUEVO

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

/**
 * Controlador del módulo de Salidas / Distribución.
 * • Registra la SALIDA_DISTRIBUCION en inventario global.
 * • Crea el stock inicial en inventario_repartidor para que el modal de notas
 *   encuentre empaques disponibles inmediatamente después de la salida.
 */

public class SalidaServlet extends HttpServlet {

    /* ===== DAOs ===== */
    private SalidaDAO                 salidaDAO;
    private RepartidorDAO             repartidorDAO;
    private CatalogoEmpaqueDAO        empaqueDAO;
    private InventarioEmpaquetadoDAO  inventarioDAO;
    private InventarioRepartidorDAO   invRepDAO;         // ← NUEVO

    /* --------------------------------------------------------- */
    /* init                                                      */
    /* --------------------------------------------------------- */
    @Override
    public void init() throws ServletException {
        try {
            salidaDAO      = new SalidaDAO();
            repartidorDAO  = new RepartidorDAO();
            empaqueDAO     = new CatalogoEmpaqueDAO();
            inventarioDAO  = new InventarioEmpaquetadoDAO();
            invRepDAO      = new InventarioRepartidorDAO();   // ← NUEVO
        } catch (SQLException e) {
            throw new ServletException("Error al inicializar DAOs", e);
        }
    }

    /* --------------------------------------------------------- */
    /* GET                                                       */
    /* --------------------------------------------------------- */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        /* ---------- AJAX: obtener empaques en JSON ---------- */
        if ("obtenerEmpaques".equals(accion)) {
            try { cargarEmpaques(response); }
            catch (SQLException e) {
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            }
            return;
        }

        try {
            if (accion == null || accion.isEmpty() || "calendario".equals(accion)) {
                List<Salida> salidas = salidaDAO.listarSalidas();
                request.setAttribute("salidas", salidas);
                request.getRequestDispatcher("/jsp/salidas/salidaCalendario.jsp")
                       .forward(request, response);

            } else if ("listar".equals(accion)) {
                List<Salida> salidas = salidaDAO.listarSalidas();
                request.setAttribute("salidas", salidas);
                request.getRequestDispatcher("/jsp/salidas/salidaList.jsp")
                       .forward(request, response);

            } else if ("nuevo".equals(accion)) {
                /* Formulario nueva salida */
                List<Repartidor>        repartidores = repartidorDAO.listar();
                List<CatalogoEmpaque>   empaques     = empaqueDAO.findAll();
                Map<Integer, Integer>   stockMap     = new HashMap<>();
                for (CatalogoEmpaque em : empaques) {
                    stockMap.put(em.getIdEmpaque(),
                                 inventarioDAO.obtenerCantidadActual(em.getIdEmpaque()));
                }
                /* filtra solo empaques con stock > 0 */
                List<CatalogoEmpaque> empaquesConStock = new ArrayList<>();
                Map<Integer, Integer> stockFiltrado    = new HashMap<>();
                for (CatalogoEmpaque e : empaques) {
                    int stock = stockMap.getOrDefault(e.getIdEmpaque(), 0);
                    if (stock > 0) {
                        empaquesConStock.add(e);
                        stockFiltrado.put(e.getIdEmpaque(), stock);
                    }
                }
                request.setAttribute("repartidores", repartidores);
                request.setAttribute("empaques",     empaquesConStock);
                
                request.setAttribute("stockMap",     stockFiltrado);
                request.getRequestDispatcher("/jsp/salidas/salidaForm.jsp")
                       .forward(request, response);

            } else if ("editar".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Salida salida = salidaDAO.buscarPorId(id);
                if (salida == null) {
                    request.getSession().setAttribute("mensaje", "Salida no encontrada.");
                    response.sendRedirect("SalidaServlet");
                    return;
                }
                List<Repartidor>      repartidores = repartidorDAO.listar();
                List<CatalogoEmpaque> empaques     = empaqueDAO.findAll();
                request.setAttribute("repartidores", repartidores);
                request.setAttribute("empaques",     empaques);
                request.setAttribute("salida",       salida);
                request.getRequestDispatcher("/jsp/salidas/salidaEditar.jsp")
                       .forward(request, response);

            } else if ("editarMultiple".equals(accion)) {
                int    idR   = Integer.parseInt(request.getParameter("idRepartidor"));
                String fecha = request.getParameter("fecha");

                List<Salida> detalles = salidaDAO.listarSalidasPorRepartidorYFecha(idR, fecha);
                String nombreR = detalles.isEmpty() ? ""
                                  : detalles.get(0).getNombreRepartidor() + " "
                                  + detalles.get(0).getApellidoRepartidor();

                request.setAttribute("detalles",         detalles);
                request.setAttribute("idRepartidor",     idR);
                request.setAttribute("fechaSeleccionada", fecha);
                request.setAttribute("nombreRepartidor", nombreR);
                request.getRequestDispatcher("/jsp/salidas/salidaEditarMultiple.jsp")
                       .forward(request, response);
                return;

            } else if ("eliminarSalida".equals(accion)) {
                /* ... tu código de eliminación original ... */

            } 
       
            // SalidaServlet.java  (dentro de doGet)
else if ("verDia".equals(accion)) {
    String fechaStr = request.getParameter("fecha");           // yyyy-MM-dd

    List<Salida> lista = salidaDAO.listarSalidasPorFecha(fechaStr);

    /* Agrupa por repartidor → Map<Integer, List<Salida>> */
    Map<Integer, List<Salida>> map = new LinkedHashMap<>();
    for (Salida s : lista) {
        map.computeIfAbsent(s.getIdRepartidor(),
                            k -> new ArrayList<>()).add(s);
    }

    /* usa los mismos nombres que el JSP */
    request.setAttribute("salidasPorRepartidor", map);
    request.setAttribute("fechaSeleccionada",     fechaStr);

    request.getRequestDispatcher("/jsp/salidas/salidasDia.jsp")
           .forward(request, response);
    return;
}                  
            else if ("verDetalle".equals(accion)) {
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
                /* default => listado */
                List<Salida> salidas = salidaDAO.listarSalidas();
                request.setAttribute("salidas", salidas);
                request.getRequestDispatcher("/jsp/salidas/salidaList.jsp")
                       .forward(request, response);
            }

        } catch (Exception e) {
            request.setAttribute("mensajeError", "Error: " + e.getMessage());
            request.getRequestDispatcher("/jsp/salidas/salidaList.jsp").forward(request, response);
        }
    }

    /* --------------------------------------------------------- */
    /* POST                                                      */
    /* --------------------------------------------------------- */
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
                    response.sendRedirect(request.getContextPath()+"/SalidaServlet?accion=nuevo&inFrame=1");
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
                response.sendRedirect(request.getContextPath()+"/SalidaServlet?accion=nuevo&inFrame=1");
                return;
            }
        } catch (Exception e) {
            request.getSession().setAttribute("mensaje", "Error al procesar: " + e.getMessage());
            response.sendRedirect("SalidaServlet");
        }
    }

    /* ========================================================= */
    /* MÉTODOS PRIVADOS                                          */
    /* ========================================================= */

    /**
     * Registra una o varias líneas de distribución,
     * crea el stock inicial del repartidor y mueve inventario global.
     */
    private void registrarSalidas(HttpServletRequest req, HttpServletResponse res)
            throws IOException, SQLException {

        int      idRepartidor   = Integer.parseInt(req.getParameter("idRepartidor"));
        String[] empaquesArr    = req.getParameterValues("idEmpaque[]");
        String[] cantidadesArr  = req.getParameterValues("cantidad[]");

        if (empaquesArr == null || cantidadesArr == null
            || empaquesArr.length != cantidadesArr.length) {

            req.getSession().setAttribute("mensaje",
                    "Error: Debes seleccionar al menos un producto y cantidad.");
            res.sendRedirect(req.getContextPath()+"/SalidaServlet?accion=nuevo&inFrame=1");
            return;
        }

        for (int i = 0; i < empaquesArr.length; i++) {
            int idEmpaque = Integer.parseInt(empaquesArr[i]);
            int cantidad  = Integer.parseInt(cantidadesArr[i]);
            if (cantidad <= 0) continue;

            /* 1. Inserta la distribución */
            Salida s = new Salida();
            s.setIdRepartidor       (idRepartidor);
            s.setIdEmpaque          (idEmpaque);
            s.setCantidad           (cantidad);
            s.setFechaDistribucion  (LocalDateTime.now());

            int idDistribucion = salidaDAO.registrarSalida(s);

            /* 2. Crea / acumula stock en inventario_repartidor ---------- */
            invRepDAO.insertarInicial(idRepartidor, idEmpaque, cantidad);

            /* 3. Registra el movimiento global -------------------------- */
            inventarioDAO.registrarMovimientoSalida(
                    idEmpaque,           // empaque
                    cantidad,            // piezas
                    idDistribucion,      // FK distribucion
                    idRepartidor,        // repartidor
                    "SALIDA_DISTRIBUCION");
        }

        req.getSession().setAttribute("mensaje", "Salida registrada correctamente.");
        /* Redirige al mismo formulario para recargar inventario disponible */
        res.sendRedirect(req.getContextPath()+"/SalidaServlet?accion=nuevo&inFrame=1");
    }

    /* ---------- AJAX: lista de empaques en JSON --------------- */
    private void cargarEmpaques(HttpServletResponse response)
            throws IOException, SQLException {

        response.setContentType("application/json");
        List<CatalogoEmpaque> empaques = empaqueDAO.findAll();

        try (PrintWriter out = response.getWriter()) {
            out.println("[");
            for (int i = 0; i < empaques.size(); i++) {
                CatalogoEmpaque emp = empaques.get(i);
                out.printf("{\"id\":%d,\"nombre\":\"%s\"}",
                           emp.getIdEmpaque(), emp.getNombreEmpaque());
                if (i < empaques.size() - 1) out.print(",");
            }
            out.println("]");
        }
    }
}
