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

            } else if ("verDia".equals(accion)) {
                /* ... tu código original ... */

            } else if ("verDetalle".equals(accion)) {
                /* ... tu código original ... */

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if ("eliminarArticulo".equals(accion)) {
            /* ... tu lógica de eliminar detalle ... */
            return;
        }

        try {
            if ("actualizar".equals(accion)) {
                /* ... actualizar línea individual ... */
                return;

            } else if ("actualizarMultiple".equals(accion)) {
                /* ... actualizar varias líneas ... */
                return;

            } else {                       /* ---------- REGISTRAR NUEVA SALIDA ---------- */
                registrarSalidas(request, response);
                return;
            }
        } catch (Exception e) {
            request.getSession().setAttribute("mensaje",
                    "Error al procesar: " + e.getMessage());
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
            res.sendRedirect("SalidaServlet?accion=nuevo");
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
        res.sendRedirect("SalidaServlet?accion=nuevo");
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
