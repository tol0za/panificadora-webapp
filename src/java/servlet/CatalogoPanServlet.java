package servlet;

import dao.CatalogoPanDAO;
import modelo.CatalogoPan;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


public class CatalogoPanServlet extends HttpServlet {
    private CatalogoPanDAO panDAO;

    @Override
    public void init() throws ServletException {
        try {
            panDAO = new CatalogoPanDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al conectar con la base de datos", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!verificarAdmin(req, resp)) return;

        String accion = req.getParameter("accion");
        if (accion == null) accion = "listar";

        try {
            switch (accion) {
                case "nuevo":
                    mostrarFormulario(req, resp, null);
                    break;
                case "editar":
                    int idEditar = Integer.parseInt(req.getParameter("id"));
                    CatalogoPan panEditar = panDAO.findById(idEditar);
                    mostrarFormulario(req, resp, panEditar);
                    break;
                case "eliminar":
                    int idEliminar = Integer.parseInt(req.getParameter("id"));
                    panDAO.delete(idEliminar);
                    req.getSession().setAttribute("mensaje", "eliminado");
                    resp.sendRedirect("CatalogoPanServlet");
                    break;
                default:
                    listar(req, resp);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, CatalogoPan pan)
            throws ServletException, IOException {
        req.setAttribute("pan", pan);
        req.getRequestDispatcher("/jsp/pan/panForm.jsp").forward(req, resp);
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        List<CatalogoPan> lista = panDAO.findAll();
        req.setAttribute("listaPan", lista);
        req.getRequestDispatcher("/jsp/pan/panList.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!verificarAdmin(req, resp)) return;

        String id = req.getParameter("idPan");
        String nombre = req.getParameter("nombrePan");

        CatalogoPan pan = new CatalogoPan();
        pan.setNombrePan(nombre);

        try {
            HttpSession session = req.getSession();
            if (id == null || id.isEmpty()) {
                panDAO.insert(pan);
                session.setAttribute("mensaje", "registrado");
            } else {
                pan.setIdPan(Integer.parseInt(id));
                panDAO.update(pan);
                session.setAttribute("mensaje", "actualizado");
            }
            resp.sendRedirect("CatalogoPanServlet");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // 🔒 Método de seguridad por rol (usando forward para no romper el layout con iframe)
private boolean verificarAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    HttpSession sesion = request.getSession(false);
    String rol = (sesion != null) ? (String) sesion.getAttribute("rol") : null;

    if (rol == null || !rol.equals("administrador")) {
        sesion.setAttribute("accesoDenegado", true);
        request.getRequestDispatcher("/jsp/home/inicio.jsp").forward(request, response);
        return false;
    }
    return true;
}
}
