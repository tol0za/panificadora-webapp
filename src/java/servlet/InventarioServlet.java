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
import java.util.List;


public class InventarioServlet extends HttpServlet {
    private InventarioEmpaquetadoDAO inventarioDAO;
    private CatalogoEmpaqueDAO empaqueDAO;

    @Override
    public void init() throws ServletException {
        try {
            inventarioDAO = new InventarioEmpaquetadoDAO();
            empaqueDAO = new CatalogoEmpaqueDAO();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        try {
            switch (accion) {
                case "nuevo":
                    List<CatalogoEmpaque> empaques = empaqueDAO.findAll();
                    request.setAttribute("empaques", empaques);
                    request.getRequestDispatcher("/jsp/inventario/inventarioForm.jsp").forward(request, response);
                    break;
                case "listar":
                default:
                    List<InventarioEmpaquetado> movimientos = inventarioDAO.listarMovimientos();
                    List<CatalogoEmpaque> empaquesList = empaqueDAO.findAll();
                    request.setAttribute("movimientos", movimientos);
                    request.setAttribute("empaques", empaquesList);
                    request.getRequestDispatcher("/jsp/inventario/inventarioList.jsp").forward(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int idEmpaque = Integer.parseInt(request.getParameter("idEmpaque"));
            int cantidad = Integer.parseInt(request.getParameter("cantidad"));
            String motivo = request.getParameter("motivo");

            if ("Salida de Mercancía".equals(motivo) || "Merma de Mercancía".equals(motivo)) {
                cantidad = -Math.abs(cantidad);
            } else {
                cantidad = Math.abs(cantidad);
            }

            int cantidadActual = inventarioDAO.obtenerCantidadActual(idEmpaque);
            int nuevaCantidad = cantidadActual + cantidad;

            InventarioEmpaquetado movimiento = new InventarioEmpaquetado();
            movimiento.setIdEmpaque(idEmpaque);
            movimiento.setCantidad(cantidad);
            movimiento.setFecha(LocalDateTime.now());
            movimiento.setMotivo(motivo);
            movimiento.setCantidadActual(nuevaCantidad);

            inventarioDAO.registrarMovimiento(movimiento);
            request.getSession().setAttribute("mensaje", "Movimiento registrado correctamente.");
        } catch (Exception e) {
            request.getSession().setAttribute("mensaje", "Error al registrar movimiento: " + e.getMessage());
        }
        response.sendRedirect("InventarioServlet");
    }
}
