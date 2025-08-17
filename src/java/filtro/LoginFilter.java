package filtro;

import dao.RepartidorDAO;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class LoginFilter implements Filter {

    /*  Rutas públicas que NO requieren sesión  */
    private static final String[] ALLOW_PUBLIC = {
        "/jsp/login/",        // carpeta donde vive tu login.jsp
        "/LoginServlet",
        "/css/", "/img/", "/static/", "/js/", "/favicon.ico"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession         session  = request.getSession(false);

        String ctx  = request.getContextPath();
        String path = request.getRequestURI().substring(ctx.length());

        /* 1. Deja pasar recursos públicos */
        for (String p : ALLOW_PUBLIC) {
            if (path.startsWith(p)) {
                chain.doFilter(req, res);
                return;
            }
        }

        /* 2. Usuario logueado? */
        if (session != null && session.getAttribute("usuario") != null) {

            /* Cargar listaRepartidores una sola vez por sesión */
            if (session.getAttribute("listaRepartidores") == null) {
                try {
                    session.setAttribute("listaRepartidores",
                        new RepartidorDAO().listar());   // usa el método que sí existe
                } catch (Exception ex) {
                    throw new ServletException("No se pudo cargar listaRepartidores", ex);
                }
            }

            chain.doFilter(req, res);   // continuar flujo normal
            return;
        }

        /* 3. No autenticado → redirige a login */
        response.sendRedirect(ctx + "/jsp/login/login.jsp");  // ruta correcta
    }

    @Override public void init(FilterConfig f) {}
    @Override public void destroy() {}
}
