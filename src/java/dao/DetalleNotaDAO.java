package dao;

import conexion.Conexion;
import modelo.DetalleNotaVenta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleNotaDAO {

    private Connection getConn() throws SQLException { return Conexion.getConnection(); }

    /** Inserta detalle. OJO: total_linea es columna GENERADA en la BD; NO se inserta explícitamente. */
    public void insertar(DetalleNotaVenta d) throws SQLException {
        final String sql =
            "INSERT INTO detalle_nota_venta " +
            "(id_nota, id_distribucion, id_empaque, cantidad_vendida, merma, precio_unitario) " +
            "VALUES (?,?,?,?,?,?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, d.getIdNota());
            // id_distribucion puede venir nulo; si 0 → NULL
            if (d.getIdDistribucion() > 0) ps.setInt(2, d.getIdDistribucion());
            else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, d.getIdEmpaque());
            ps.setInt(4, d.getCantidadVendida());
            ps.setInt(5, d.getMerma());
            ps.setDouble(6, d.getPrecioUnitario());
            ps.executeUpdate();
        }
    }

    /** Elimina todos los renglones de una nota */
    public void eliminarPorNota(int idNota) throws SQLException {
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM detalle_nota_venta WHERE id_nota=?")) {
            ps.setInt(1, idNota);
            ps.executeUpdate();
        }
    }

    /** Lista detalle. Calcula el total de línea con COBRADAS (vendidas - merma) * precio */
    public List<DetalleNotaVenta> listarPorNota(int idNota) throws SQLException {
        List<DetalleNotaVenta> list = new ArrayList<>();
        final String sql =
            "SELECT id_detalle, id_nota, id_distribucion, id_empaque, " +
            "       cantidad_vendida, IFNULL(merma,0) merma, precio_unitario " +
            "FROM detalle_nota_venta WHERE id_nota=? " +
            "ORDER BY id_detalle";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleNotaVenta d = new DetalleNotaVenta();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setIdNota(rs.getInt("id_nota"));
                    d.setIdDistribucion(rs.getInt("id_distribucion"));
                    d.setIdEmpaque(rs.getInt("id_empaque"));
                    d.setCantidadVendida(rs.getInt("cantidad_vendida"));
                    d.setMerma(rs.getInt("merma"));
                    d.setPrecioUnitario(rs.getDouble("precio_unitario"));

                    int cobradas = Math.max(0, d.getCantidadVendida() - d.getMerma());
                    d.setTotalLinea(cobradas * d.getPrecioUnitario()); // para la UI
                    list.add(d);
                }
            }
        }
        return list;
    }

    /** Total de la nota = SUM( (vendidas - merma) * precio )  → total neto cobrado */
    public double obtenerTotalPorNota(int idNota) throws SQLException {
        final String sql =
            "SELECT COALESCE(SUM( (cantidad_vendida - IFNULL(merma,0)) * precio_unitario ),0) " +
            "FROM detalle_nota_venta WHERE id_nota=?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }
}
