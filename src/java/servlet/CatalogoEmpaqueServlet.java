// CatalogoEmpaqueServlet.java
package servlet;

import dao.CatalogoEmpaqueDAO;
import modelo.CatalogoEmpaque;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.List;

public class CatalogoEmpaqueServlet extends HttpServlet {
    private CatalogoEmpaqueDAO dao;

    @Override
    public void init() throws ServletException {
        try {
            dao = new CatalogoEmpaqueDAO();
        } catch (Exception e) {
            throw new ServletException("Error de conexión", e);
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
                    int id = Integer.parseInt(req.getParameter("id"));
                    CatalogoEmpaque e = dao.findById(id);
                    mostrarFormulario(req, resp, e);
                    break;
                case "eliminar":
                    int idEliminar = Integer.parseInt(req.getParameter("id"));
                    dao.delete(idEliminar);
                    req.getSession().setAttribute("mensaje", "eliminado");
                    resp.sendRedirect("CatalogoEmpaqueServlet");
                    break;
                default:
                    List<CatalogoEmpaque> lista = dao.findAll();
                    req.setAttribute("listaEmpaques", lista);
                    req.getRequestDispatcher("/jsp/empaques/empaqueList.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!verificarAdmin(req, resp)) return;

        String id = req.getParameter("idEmpaque");
        String nombre = req.getParameter("nombreEmpaque");
        String precioStr = req.getParameter("precioUnitario");

        CatalogoEmpaque e = new CatalogoEmpaque();
        e.setNombreEmpaque(nombre);
        e.setPrecioUnitario(new BigDecimal(precioStr));

        try {
            if (id == null || id.isEmpty()) {
                dao.insert(e);
                req.getSession().setAttribute("mensaje", "registrado");
            } else {
                e.setIdEmpaque(Integer.parseInt(id));
                dao.update(e);
                req.getSession().setAttribute("mensaje", "actualizado");
            }
            resp.sendRedirect("CatalogoEmpaqueServlet");
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, CatalogoEmpaque e)
            throws ServletException, IOException {
        req.setAttribute("empaque", e);
        req.getRequestDispatcher("/jsp/empaques/empaqueForm.jsp").forward(req, resp);
    }

    private boolean verificarAdmin(HttpServletRequest request, HttpServletResponse response) {
        HttpSession sesion = request.getSession(false);
        String rol = (sesion != null) ? (String) sesion.getAttribute("rol") : null;
        if (rol == null || !rol.equals("administrador")) {
            sesion.setAttribute("accesoDenegado", true);
            try {
                request.getRequestDispatcher("/jsp/home/inicio.jsp").forward(request, response);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
        return true;
    }
}