package dao;
import modelo.CatalogoEmpaque;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import conexion.Conexion;

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

    // --- MÉTODO para compatibilidad con NotaVenta ---
    public List<CatalogoEmpaque> obtenerEmpaquesPorRepartidorYFecha(int idRepartidor, java.sql.Date fecha) throws SQLException {
        List<CatalogoEmpaque> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT e.id_empaque, e.nombre_empaque, e.precio_unitario " +
                "FROM catalogo_empaque e " +
                "INNER JOIN distribucion d ON e.id_empaque = d.id_empaque " +
                "WHERE d.id_repartidor=? AND DATE(d.fecha_distribucion)=?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRepartidor);
            stmt.setDate(2, fecha);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CatalogoEmpaque empaque = new CatalogoEmpaque();
                    empaque.setIdEmpaque(rs.getInt("id_empaque"));
                    empaque.setNombreEmpaque(rs.getString("nombre_empaque"));
                    empaque.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    lista.add(empaque);
                }
            }
        }
        return lista;
    }
}
