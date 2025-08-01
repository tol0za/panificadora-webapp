package dao;

import conexion.Conexion;
import modelo.InventarioEmpaquetado;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

public class InventarioEmpaquetadoDAO {
    private Connection con;

    public InventarioEmpaquetadoDAO() throws SQLException {
        con = Conexion.getConnection();
    }

    public List<InventarioEmpaquetado> listarMovimientos() throws SQLException {
        String sql = "SELECT i.*, c.nombre_empaque FROM inventario_empaquetado i " +
                     "JOIN catalogo_empaque c ON i.id_empaque = c.id_empaque " +
                     "ORDER BY i.fecha DESC";

        List<InventarioEmpaquetado> lista = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventarioEmpaquetado ie = new InventarioEmpaquetado();
                ie.setIdInventario(rs.getInt("id_inventario"));
                ie.setIdEmpaque(rs.getInt("id_empaque"));
                ie.setNombreEmpaque(rs.getString("nombre_empaque"));
                ie.setCantidad(rs.getInt("cantidad"));
                ie.setMotivo(rs.getString("motivo"));
                ie.setCantidadActual(rs.getInt("cantidad_actual"));

                Timestamp timestamp = rs.getTimestamp("fecha");
                if (timestamp != null) {
                    ie.setFecha(timestamp.toLocalDateTime());
                    ie.setFechaDate(Date.from(ie.getFecha().atZone(ZoneId.systemDefault()).toInstant()));
                }
                lista.add(ie);
            }
        }
        return lista;
    }

    public void registrarMovimiento(InventarioEmpaquetado movimiento) throws SQLException {
        String sqlInsert = "INSERT INTO inventario_empaquetado (id_empaque, cantidad, fecha, motivo, cantidad_actual) " +
                           "VALUES (?, ?, ?, ?, ?)";
        con.setAutoCommit(false);

        try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
            psInsert.setInt(1, movimiento.getIdEmpaque());
            psInsert.setInt(2, movimiento.getCantidad());
            psInsert.setTimestamp(3, Timestamp.valueOf(movimiento.getFecha()));
            psInsert.setString(4, movimiento.getMotivo());
            psInsert.setInt(5, movimiento.getCantidadActual());

            psInsert.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    /** Stock real basado en último movimiento */
    public int obtenerCantidadActual(int idEmpaque) throws SQLException {
        String sql = "SELECT cantidad_actual FROM inventario_empaquetado WHERE id_empaque = ? ORDER BY fecha DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpaque);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cantidad_actual") : 0;
            }
        }
    }

    // --- Métodos adicionales útiles ---
    public BigDecimal obtenerPrecioUnitario(int idEmpaque) throws SQLException {
        String sql = "SELECT precio_unitario FROM catalogo_empaque WHERE id_empaque = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpaque);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("precio_unitario");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public void ajustarInventario(int idEmpaque, int delta, String motivo) throws SQLException {
        int actual = obtenerCantidadActual(idEmpaque);
        int nuevo = actual + delta;
        String sqlInsert = "INSERT INTO inventario_empaquetado (id_empaque, cantidad, fecha, motivo, cantidad_actual) " +
                           "VALUES (?, ?, NOW(), ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
            ps.setInt(1, idEmpaque);
            ps.setInt(2, delta);
            ps.setString(3, motivo);
            ps.setInt(4, nuevo);
            ps.executeUpdate();
        }
    }
    public int obtenerStockActual(int idEmpaque) throws SQLException {
    String sql = "SELECT cantidad_actual FROM inventario_empaquetado WHERE id_empaque = ? ORDER BY fecha DESC LIMIT 1";
    try (PreparedStatement stmt = con.prepareStatement(sql)) {
        stmt.setInt(1, idEmpaque);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("cantidad_actual");
        }
    }
    return 0;
}
}
