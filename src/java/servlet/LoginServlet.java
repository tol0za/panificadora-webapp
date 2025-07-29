package servlet;

import modelo.Usuario;
import dao.UsuarioDAO;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class LoginServlet extends HttpServlet {
    private UsuarioDAO dao = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String user = req.getParameter("usuario");
        String pass = req.getParameter("password");

        try {
            Usuario u = dao.validar(user, pass);

            if (u != null) {
                HttpSession ses = req.getSession();
                ses.setAttribute("usuario", u);
                ses.setAttribute("rol", u.getRol()); // ✅ ESTO FALTABA

                resp.sendRedirect(req.getContextPath() + "/jsp/home/inicio.jsp");
            } else {
                req.setAttribute("error", "Usuario o contraseña incorrectos");
                req.getRequestDispatcher("/jsp/login/login.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/jsp/login/login.jsp");
    }
}
