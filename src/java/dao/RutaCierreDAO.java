package dao;

import conexion.Conexion;
import java.sql.*;
import java.time.LocalDate;

public class RutaCierreDAO {
    private Connection getConn() throws SQLException { return Conexion.getConnection(); }

    /** true si la ruta ya está cerrada para ese repartidor en esa fecha */
    public boolean estaCerrada(int idRep, LocalDate fecha) throws SQLException {
        String sql = "SELECT 1 FROM ruta_cierre WHERE id_repartidor=? AND fecha=? LIMIT 1";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /** Cierra (idempotente) */
    public void cerrar(int idRep, LocalDate fecha) throws SQLException {
        String sql = "INSERT INTO ruta_cierre (id_repartidor, fecha) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE cerrado_en = NOW()";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            ps.executeUpdate();
        }
    }

    /** Reabre (borra la marca) */
    public void reabrir(int idRep, LocalDate fecha) throws SQLException {
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM ruta_cierre WHERE id_repartidor=? AND fecha=?")) {
            ps.setInt(1, idRep);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            ps.executeUpdate();
        }
    }
}
