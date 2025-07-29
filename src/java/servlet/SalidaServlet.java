package servlet;

import dao.CatalogoEmpaqueDAO;
import dao.RepartidorDAO;
import dao.SalidaDAO;
import modelo.CatalogoEmpaque;
import modelo.Repartidor;
import modelo.Salida;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class SalidaServlet extends HttpServlet {
    private SalidaDAO salidaDAO;

    @Override
    public void init() throws ServletException {
        try {
            salidaDAO = new SalidaDAO();
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
                    cargarDatosFormulario(request);
                    request.getRequestDispatcher("/jsp/salidas/salidaForm.jsp").forward(request, response);
                    break;
                case "listar":
                default:
                    List<Salida> salidas = salidaDAO.listarSalidas();

                    // Convertir LocalDateTime a Date para JSP
                    for (Salida salida : salidas) {
                        LocalDateTime ldt = salida.getFechaDistribucion();
                        if (ldt != null) {
                            Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                            salida.setFechaDistribucionDate(date);
                        }
                    }

                    request.setAttribute("salidas", salidas);
                    request.getRequestDispatcher("/jsp/salidas/salidaList.jsp").forward(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int idRepartidor = Integer.parseInt(request.getParameter("idRepartidor"));
            int idEmpaque = Integer.parseInt(request.getParameter("idEmpaque"));
            int cantidad = Integer.parseInt(request.getParameter("cantidad"));

            Salida salida = new Salida(0, idRepartidor, idEmpaque, cantidad, LocalDateTime.now());
            salidaDAO.insertarSalida(salida);
            request.getSession().setAttribute("mensaje", "Salida registrada correctamente.");
        } catch (Exception e) {
            request.getSession().setAttribute("mensaje", "Error al registrar salida: " + e.getMessage());
        }
        response.sendRedirect("SalidaServlet");
    }

    private void cargarDatosFormulario(HttpServletRequest request) throws SQLException {
        RepartidorDAO repartidorDAO = new RepartidorDAO();
        CatalogoEmpaqueDAO empaqueDAO = new CatalogoEmpaqueDAO();

        List<Repartidor> repartidores = repartidorDAO.listarTodos();
        List<CatalogoEmpaque> empaques = empaqueDAO.findAll();

        request.setAttribute("repartidores", repartidores);
        request.setAttribute("empaques", empaques);
    }
}
