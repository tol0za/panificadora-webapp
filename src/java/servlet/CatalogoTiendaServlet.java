package servlet;

import dao.TiendaDAO;
import modelo.Tienda;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/CatalogoTiendaServlet")
public class CatalogoTiendaServlet extends HttpServlet {
    private TiendaDAO tiendaDAO;

    @Override
    public void init() throws ServletException {
        try {
            tiendaDAO = new TiendaDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al conectar con la base de datos", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession(false);
        String rol = (sesion != null) ? (String) sesion.getAttribute("rol") : null;

        if (rol == null) {
            req.getRequestDispatcher("/jsp/login/login.jsp").forward(req, resp);
            return;
        }

        String accion = req.getParameter("accion");
        if (accion == null) accion = "listar";

        try {
            switch (accion) {
                case "nuevo":
                    req.getRequestDispatcher("/jsp/tiendas/tiendaForm.jsp").forward(req, resp);
                    break;
                case "editar":
                    int idEditar = Integer.parseInt(req.getParameter("id"));
                    Tienda tiendaEditar = tiendaDAO.buscarPorId(idEditar);
                    req.setAttribute("tienda", tiendaEditar);
                    req.getRequestDispatcher("/jsp/tiendas/tiendaForm.jsp").forward(req, resp);
                    break;
                case "eliminar":
                    int idEliminar = Integer.parseInt(req.getParameter("id"));
                    tiendaDAO.eliminar(idEliminar);
                    req.getSession().setAttribute("mensaje", "Tienda eliminada correctamente");
                    resp.sendRedirect(req.getContextPath() + "/CatalogoTiendaServlet");
                    break;
                default:
                    List<Tienda> lista = tiendaDAO.listar();
                    req.setAttribute("listaTiendas", lista);
                    req.getRequestDispatcher("/jsp/tiendas/tiendaList.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            throw new ServletException("Error al procesar solicitud de tiendas", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int id = (req.getParameter("idTienda") == null || req.getParameter("idTienda").isEmpty())
                     ? 0 : Integer.parseInt(req.getParameter("idTienda"));
            String nombre = req.getParameter("nombre");
            String direccion = req.getParameter("direccion");
            String telefono = req.getParameter("telefono");

            Tienda tienda = new Tienda(id, nombre, direccion, telefono);

            if (id == 0) {
                tiendaDAO.insertar(tienda);
                req.getSession().setAttribute("mensaje", "Tienda registrada correctamente");
            } else {
                tiendaDAO.actualizar(tienda);
                req.getSession().setAttribute("mensaje", "Tienda actualizada correctamente");
            }

            resp.sendRedirect(req.getContextPath() + "/CatalogoTiendaServlet");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                req.getSession().setAttribute("mensaje", "Ya existe una tienda con ese nombre");
                resp.sendRedirect(req.getContextPath() + "/CatalogoTiendaServlet");
            } else {
                throw new ServletException("Error al procesar el formulario de tienda", e);
            }
        }
    }
}
