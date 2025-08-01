package servlet;

import dao.RepartidorDAO;
import modelo.Repartidor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


public class RepartidorServlet extends HttpServlet {

    private RepartidorDAO repartidorDAO;

    @Override
    public void init() throws ServletException {
        try {
            repartidorDAO = new RepartidorDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al conectar con la base de datos", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // ⛔ Verificación de rol
        if (!verificarAdmin(request)) {
            session.setAttribute("accesoDenegado", true);
            request.getRequestDispatcher("/jsp/home/inicio.jsp").forward(request, response);
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        try {
            switch (accion) {
                case "nuevo":
                    mostrarFormulario(request, response, null);
                    break;
                case "editar":
                    mostrarFormularioEdicion(request, response);
                    break;
                case "eliminar":
                    eliminarRepartidor(request, response);
                    break;
                default:
                    listarRepartidores(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // ⛔ Verificación de rol
        if (!verificarAdmin(request)) {
            session.setAttribute("accesoDenegado", true);
            request.getRequestDispatcher("/jsp/home/inicio.jsp").forward(request, response);
            return;
        }

        String idStr = request.getParameter("idRepartidor");
        String nombre = request.getParameter("nombreRepartidor");
        String apellido = request.getParameter("apellidoRepartidor");
        String telefono = request.getParameter("telefono");

        Repartidor r = new Repartidor();
        r.setNombreRepartidor(nombre);
        r.setApellidoRepartidor(apellido);
        r.setTelefono(telefono);

        try {
            if (idStr == null || idStr.isEmpty()) {
                repartidorDAO.insertar(r);
                session.setAttribute("mensaje", "registrado");
            } else {
                r.setIdRepartidor(Integer.parseInt(idStr));
                repartidorDAO.actualizar(r);
                session.setAttribute("mensaje", "actualizado");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        response.sendRedirect(request.getContextPath() + "/RepartidorServlet");
    }

    private void listarRepartidores(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        // AJUSTA EL MÉTODO segun tu DAO real
        List<Repartidor> lista = repartidorDAO.listar();
        request.setAttribute("listaRepartidores", lista);
        request.getRequestDispatcher("/jsp/admin/repartidorList.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, Repartidor r)
            throws ServletException, IOException {
        request.setAttribute("repartidor", r);
        request.getRequestDispatcher("/jsp/repartidores/repartidorForm.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Repartidor r = repartidorDAO.buscarPorId(id);
        mostrarFormulario(request, response, r);
    }

    private void eliminarRepartidor(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        repartidorDAO.eliminar(id);
        request.getSession().setAttribute("mensaje", "eliminado");
        response.sendRedirect(request.getContextPath() + "/RepartidorServlet");
    }

    // 🔐 Validación de rol sin redirección directa
    private boolean verificarAdmin(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        String rol = (sesion != null) ? (String) sesion.getAttribute("rol") : null;
        return (rol != null && rol.equals("administrador"));
    }
}
