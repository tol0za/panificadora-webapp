package servlet;

import dao.InventarioEmpaquetadoDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;


public class StockServlet extends HttpServlet {

    private InventarioEmpaquetadoDAO inventarioDAO;

    @Override
    public void init() throws ServletException {
        try {
            inventarioDAO = new InventarioEmpaquetadoDAO();
        } catch (SQLException e) {
            throw new ServletException("Error al inicializar DAO de inventario", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idEmpaqueStr = request.getParameter("id_empaque");

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        try {
            int idEmpaque = Integer.parseInt(idEmpaqueStr);
            int stock = inventarioDAO.obtenerStockActual(idEmpaque);
            out.print(stock);
        } catch (NumberFormatException | SQLException e) {
            out.print("Error");
        }
    }
}
