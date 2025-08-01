
package servlet;

import com.google.gson.Gson;
import dao.SalidaDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;


public class EmpaquesPorRepartidorServlet extends HttpServlet {

    private SalidaDAO salidaDAO;

    @Override
    public void init() throws ServletException {
        try {
            salidaDAO = new SalidaDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al inicializar SalidaDAO", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idRepartidorStr = request.getParameter("idRepartidor");
        if (idRepartidorStr == null || idRepartidorStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta parámetro idRepartidor");
            return;
        }

        try {
            int idRepartidor = Integer.parseInt(idRepartidorStr);
            List<Map<String, Object>> empaques = salidaDAO.obtenerEmpaquesPorRepartidor(idRepartidor);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(new Gson().toJson(empaques));
            out.flush();

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "idRepartidor debe ser numérico");
        } catch (SQLException e) {
            throw new ServletException("Error al obtener empaques del repartidor", e);
        }
    }
}
