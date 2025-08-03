package dao;

import modelo.CatalogoEmpaque;
import conexion.Conexion;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class CatalogoEmpaqueDAO {

    public void insert(CatalogoEmpaque empaque) throws SQLException {
        String sql = "INSERT INTO catalogo_empaque (nombre_empaque, precio_unitario) VALUES (?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empaque.getNombreEmpaque());
            stmt.setBigDecimal(2, empaque.getPrecioUnitario());
            stmt.executeUpdate();
        }
    }

    public void update(CatalogoEmpaque empaque) throws SQLException {
        String sql = "UPDATE catalogo_empaque SET nombre_empaque = ?, precio_unitario = ? WHERE id_empaque = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empaque.getNombreEmpaque());
            stmt.setBigDecimal(2, empaque.getPrecioUnitario());
            stmt.setInt(3, empaque.getIdEmpaque());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM catalogo_empaque WHERE id_empaque = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public CatalogoEmpaque findById(int id) throws SQLException {
        String sql = "SELECT * FROM catalogo_empaque WHERE id_empaque = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CatalogoEmpaque empaque = new CatalogoEmpaque();
                    empaque.setIdEmpaque(rs.getInt("id_empaque"));
                    empaque.setNombreEmpaque(rs.getString("nombre_empaque"));
                    empaque.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    return empaque;
                }
            }
        }
        return null;
    }

    public List<CatalogoEmpaque> findAll() throws SQLException {
        List<CatalogoEmpaque> lista = new ArrayList<>();
        String sql = "SELECT * FROM catalogo_empaque ORDER BY id_empaque";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CatalogoEmpaque empaque = new CatalogoEmpaque();
                empaque.setIdEmpaque(rs.getInt("id_empaque"));
                empaque.setNombreEmpaque(rs.getString("nombre_empaque"));
                empaque.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                lista.add(empaque);
            }
        }
        return lista;
    }

    /**
     * Devuelve el precio unitario de un empaque por ID.
     */
    public BigDecimal obtenerPrecioPorId(int idEmpaque) throws SQLException {
        String sql = "SELECT precio_unitario FROM catalogo_empaque WHERE id_empaque = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEmpaque);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("precio_unitario");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Método usado por EmpaquesPorRepartidorServlet y notaForm.jsp.
     */
    public List<CatalogoEmpaque> obtenerEmpaquesPorRepartidorYFecha(int idRepartidor, java.sql.Date fecha) throws SQLException {
        List<CatalogoEmpaque> lista = new ArrayList<>();
        String sql = "SELECT d.id_empaque, c.nombre_empaque, c.precio_unitario, " +
                     "       (SELECT cantidad_actual FROM inventario_empaquetado " +
                     "        WHERE id_repartidor = d.id_repartidor AND id_empaque = d.id_empaque " +
                     "        ORDER BY fecha DESC LIMIT 1) AS stock " +
                     "FROM distribucion d " +
                     "JOIN catalogo_empaque c ON d.id_empaque = c.id_empaque " +
                     "WHERE d.id_repartidor = ? AND DATE(d.fecha_distribucion) = ? " +
                     "GROUP BY d.id_empaque";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRepartidor);
            stmt.setDate(2, fecha);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CatalogoEmpaque e = new CatalogoEmpaque();
                    e.setIdEmpaque(rs.getInt("id_empaque"));
                    e.setNombreEmpaque(rs.getString("nombre_empaque"));
                    e.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    e.setStock(rs.getInt("stock")); // Asegúrate de que esté en el modelo
                    lista.add(e);
                }
            }
        }
        return lista;
    }

    /**
     * Método requerido por EmpaquesPorRepartidorServlet.java
     */
    public CatalogoEmpaque buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM catalogo_empaque WHERE id_empaque = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CatalogoEmpaque empaque = new CatalogoEmpaque();
                    empaque.setIdEmpaque(rs.getInt("id_empaque"));
                    empaque.setNombreEmpaque(rs.getString("nombre_empaque"));
                    empaque.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    return empaque;
                }
            }
        }
        return null;
    }
}