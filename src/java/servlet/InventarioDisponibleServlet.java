package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import conexion.Conexion;

/**
 * Devuelve en JSON los empaques cuyo <strong>stock actual &gt; 0</strong>.
 * <p>Consulta el valor más reciente de <code>cantidad_actual</code> en
 * <code>inventario_empaquetado</code> y filtra los que aún tienen piezas disponibles.
 *
 * <ul>
 *   <li>Paquete <code>servlet</code> y API&nbsp;<code>javax.servlet</code>,
 *       como el resto de tu proyecto.</li>
 *   <li>Uso de <code>Conexion.getConnection()</code> – sin DataSource.</li>
 *   <li>Generación manual del JSON para evitar dependencias externas.</li>
 *   <li>Cualquier <code>SQLException</code> se propaga como
 *       <code>ServletException</code> para que tu manejador global
 *       muestre SweetAlert2.</li>
 * </ul>
 */
@WebServlet(name = "InventarioDisponibleServlet",
            urlPatterns = {"/InventarioDisponibleServlet"})
public class InventarioDisponibleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Último stock por empaque (solo los que quedan &gt; 0). */
    private static final String SQL =
        "SELECT  e.id_empaque, e.nombre_empaque,\n" +
        "        ( SELECT cantidad_actual\n" +
        "            FROM inventario_empaquetado ie\n" +
        "           WHERE ie.id_empaque = e.id_empaque\n" +
        "           ORDER BY ie.fecha DESC\n" +
        "           LIMIT 1 ) AS stock\n" +
        "  FROM catalogo_empaque e\n" +
        "  HAVING stock > 0\n" +
        "  ORDER BY e.nombre_empaque";

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
                         throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-store");

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(',');
                json.append('{')
                    .append("\"id_empaque\":").append(rs.getInt("id_empaque")).append(',')
                    .append("\"nombre_empaque\":\"")
                        .append(escape(rs.getString("nombre_empaque"))).append("\",")
                    .append("\"stock\":").append(rs.getInt("stock"))
                    .append('}');
                first = false;
            }
            json.append(']');

            resp.getWriter().write(json.toString());

        } catch (SQLException ex) {
            throw new ServletException(
                "Error al consultar inventario disponible", ex);
        }
    }

    /** Delegamos cualquier POST a GET (solo lectura). */
    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
                          throws ServletException, IOException {
        doGet(req, resp);
    }

    /* ---------- helpers ---------- */
    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}