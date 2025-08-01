package servlet;

import dao.*;
import modelo.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/NotasVentaServlet")
public class NotaVentaServlet extends HttpServlet {
    private NotaVentaDAO notaVentaDAO;
    private RepartidorDAO repartidorDAO;
    private CatalogoEmpaqueDAO empaqueDAO;
    private InventarioEmpaquetadoDAO inventarioDAO;
    private TiendaDAO tiendaDAO;
    private DetalleNotaVentaDAO detalleDAO;

    @Override
    public void init() throws ServletException {
        try {
            notaVentaDAO = new NotaVentaDAO();
            repartidorDAO = new RepartidorDAO();
            empaqueDAO = new CatalogoEmpaqueDAO();
            inventarioDAO = new InventarioEmpaquetadoDAO();
            tiendaDAO = new TiendaDAO();
            detalleDAO = new DetalleNotaVentaDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al inicializar DAOs", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        try {
            switch (accion) {
                case "nuevo":
                    cargarFormulario(request, response);
                    break;
                case "editar":
                    editarNota(request, response);
                    break;
                case "eliminar":
                    eliminarNota(request, response);
                    break;
                case "obtenerEmpaques":
                    cargarEmpaquesPorRepartidor(request, response);
                    break;
                case "listar":
                default:
                    listarNotas(request, response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private void cargarFormulario(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Repartidor> repartidores = repartidorDAO.obtenerRepartidoresConSalidaHoy();
        List<Tienda> tiendas = tiendaDAO.listar();
        request.setAttribute("repartidores", repartidores);
        request.setAttribute("tiendas", tiendas);
        request.getRequestDispatcher("/jsp/notaventa/notaForm.jsp").forward(request, response);
    }

    private void listarNotas(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LocalDate hoy = LocalDate.now();
        java.sql.Date hoySQL = java.sql.Date.valueOf(hoy);
        List<NotaVenta> notasVenta = notaVentaDAO.listarNotasPorDia(hoySQL);
        BigDecimal totalNotasDia = notaVentaDAO.totalNotasDelDia(hoySQL);
        request.setAttribute("notasVenta", notasVenta);
        request.setAttribute("totalNotasDia", totalNotasDia);
        request.getRequestDispatcher("/jsp/notaventa/notaList.jsp").forward(request, response);
    }

    private void cargarEmpaquesPorRepartidor(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int idRepartidor = Integer.parseInt(request.getParameter("idRepartidor"));
        LocalDate hoy = LocalDate.now();
        java.sql.Date hoySQL = java.sql.Date.valueOf(hoy);
        List<CatalogoEmpaque> empaques = empaqueDAO.obtenerEmpaquesPorRepartidorYFecha(idRepartidor, hoySQL);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("[");
        for (int i = 0; i < empaques.size(); i++) {
            CatalogoEmpaque e = empaques.get(i);
            out.print("{\"id\":" + e.getIdEmpaque() + ",\"nombre\":\"" + e.getNombreEmpaque() + "\",\"precio\":" + e.getPrecioUnitario() + "}");
            if (i < empaques.size() - 1) out.print(",");
        }
        out.print("]");
        out.close();
    }

    private void editarNota(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int idNota = Integer.parseInt(request.getParameter("idNota"));
        NotaVenta nv = notaVentaDAO.obtenerNotaPorId(idNota);
        List<DetalleNotaVenta> detalles = notaVentaDAO.listarDetallesPorNota(idNota);
        List<Repartidor> repartidores = repartidorDAO.listar();
        List<Tienda> tiendas = tiendaDAO.listar();
        request.setAttribute("notaVenta", nv);
        request.setAttribute("detalles", detalles);
        request.setAttribute("repartidores", repartidores);
        request.setAttribute("tiendas", tiendas);
        request.getRequestDispatcher("/jsp/notaventa/notaForm.jsp").forward(request, response);
    }

    private void eliminarNota(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int idNota = Integer.parseInt(request.getParameter("idNota"));
        detalleDAO.eliminarPorNota(idNota);
        notaVentaDAO.eliminarNotaVenta(idNota);
        request.getSession().setAttribute("mensaje", "Nota eliminada correctamente");
        response.sendRedirect(request.getContextPath() + "/NotaVentaServlet?accion=listar");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardarNota(request, response, false);
            } else if ("actualizar".equals(accion)) {
                guardarNota(request, response, true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            request.getSession().setAttribute("mensaje", "Error: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/NotaVentaServlet?accion=listar");
        }
    }

    private void guardarNota(HttpServletRequest request, HttpServletResponse response, boolean esActualizacion)
            throws Exception {
        int idNota = esActualizacion ? Integer.parseInt(request.getParameter("idNota")) : 0;
        int idRepartidor = Integer.parseInt(request.getParameter("idRepartidor"));
        int idTienda = Integer.parseInt(request.getParameter("idTienda"));
        String[] idEmpaques = request.getParameterValues("idEmpaque[]");
        String[] cantidades = request.getParameterValues("cantidad[]");
        String[] precios = request.getParameterValues("precioUnitario[]");

        BigDecimal totalNota = BigDecimal.ZERO;
        List<DetalleNotaVenta> detalles = new ArrayList<>();
        for (int i = 0; i < idEmpaques.length; i++) {
            DetalleNotaVenta det = new DetalleNotaVenta();
            det.setIdEmpaque(Integer.parseInt(idEmpaques[i]));
            int cantidad = Integer.parseInt(cantidades[i]);
            det.setCantidad(cantidad);
            BigDecimal precio = new BigDecimal(precios[i]);
            det.setPrecioUnitario(precio);
            det.setSubtotal(precio.multiply(BigDecimal.valueOf(cantidad)));
            detalles.add(det);
            totalNota = totalNota.add(det.getSubtotal());
        }

        NotaVenta nota = new NotaVenta();
        nota.setIdNota(idNota);
        nota.setIdRepartidor(idRepartidor);
        nota.setIdTienda(idTienda);
        nota.setTotal(totalNota);

        if (!esActualizacion) {
            int idNotaNueva = notaVentaDAO.insertarNotaVenta(nota);
            detalleDAO.eliminarPorNota(idNotaNueva); // Prevención doble registro
            notaVentaDAO.insertarDetalleNotaVenta(detalles, idNotaNueva);
            for (DetalleNotaVenta det : detalles) {
                inventarioDAO.ajustarInventario(det.getIdEmpaque(), -det.getCantidad(), "Venta nota");
            }
            request.getSession().setAttribute("mensaje", "Nota registrada correctamente");
        } else {
            notaVentaDAO.actualizarNotaVenta(nota);
            detalleDAO.eliminarPorNota(idNota);
            notaVentaDAO.insertarDetalleNotaVenta(detalles, idNota);
            for (DetalleNotaVenta det : detalles) {
                inventarioDAO.ajustarInventario(det.getIdEmpaque(), -det.getCantidad(), "Actualización de nota");
            }
            request.getSession().setAttribute("mensaje", "Nota actualizada correctamente");
        }
        response.sendRedirect(request.getContextPath() + "/NotaVentaServlet?accion=listar");
    }
}
