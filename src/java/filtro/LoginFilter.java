package filtro;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter("/*")
public class LoginFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getRequestURI();

        boolean loggedIn = session != null && session.getAttribute("usuario") != null;
        String rol = (loggedIn) ? (String) session.getAttribute("rol") : null;

        // Rutas públicas
        boolean accesoPublico = path.contains("login.jsp") || path.contains("LoginServlet") || path.contains("css") || path.contains("js") || path.endsWith(".png");

        // Protección general
        if (!loggedIn && !accesoPublico) {
            res.sendRedirect(req.getContextPath() + "/jsp/login/login.jsp");
            return;
        }

        // Protección de áreas administrativas
        if (path.contains("/jsp/usuarios") && !"administrador".equals(rol)) {
            res.sendRedirect(req.getContextPath() + "/jsp/home/inicio.jsp");
            return;
        }

        chain.doFilter(request, response);
    }

    public void destroy() {}
}
