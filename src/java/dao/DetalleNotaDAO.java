package dao;

import conexion.Conexion;
import modelo.DetalleNotaVenta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para detalle_nota_venta.
 *
 * 100 % NUEVO y aislado.
 */
public class DetalleNotaDAO {

    private Connection getConn() throws SQLException {
        return Conexion.getConnection();
    }

    /**
     * Inserta una línea en detalle_nota_venta.
     */
    public void insertar(DetalleNotaVenta d) throws SQLException {
        String sql = "INSERT INTO detalle_nota_venta " +
                     "(id_nota, id_distribucion, id_empaque, cantidad_vendida, merma, precio_unitario) " +
                     "VALUES (?,?,?,?,?,?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, d.getIdNota());
            ps.setInt(2, d.getIdDistribucion());
            ps.setInt(3, d.getIdEmpaque());
            ps.setInt(4, d.getCantidadVendida());
            ps.setInt(5, d.getMerma());
            ps.setDouble(6, d.getPrecioUnitario());
            ps.executeUpdate();
        }
    }

    /**
     * Elimina todas las líneas de una nota.
     */
    public void eliminarPorNota(int idNota) throws SQLException {
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(
                "DELETE FROM detalle_nota_venta WHERE id_nota=?")) {
            ps.setInt(1, idNota);
            ps.executeUpdate();
        }
    }

    /**
     * Lista detalle de una nota específica.
     */
    public List<DetalleNotaVenta> listarPorNota(int idNota) throws SQLException {
        List<DetalleNotaVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_nota_venta WHERE id_nota=?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleNotaVenta d = mapRow(rs);
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    /* ------------------------------------------------------ */
    private DetalleNotaVenta mapRow(ResultSet rs) throws SQLException {
        DetalleNotaVenta d = new DetalleNotaVenta();
        d.setIdDetalle(rs.getInt("id_detalle"));
        d.setIdNota(rs.getInt("id_nota"));
        d.setIdDistribucion(rs.getInt("id_distribucion"));
        d.setIdEmpaque(rs.getInt("id_empaque"));
        d.setCantidadVendida(rs.getInt("cantidad_vendida"));
        d.setMerma(rs.getInt("merma"));
        d.setPrecioUnitario(rs.getDouble("precio_unitario"));
        d.setTotalLinea(rs.getDouble("total_linea"));
        return d;
    }
    /* Devuelve el total calculado de la nota = Σ (vendidas * precio_unitario) */
public double obtenerTotalPorNota(int idNota) throws SQLException {
    String sql = """
        SELECT COALESCE(SUM(cantidad_vendida * precio_unitario), 0) AS total
        FROM detalle_nota_venta
        WHERE id_nota = ?""";
    try (Connection c = Conexion.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, idNota);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble("total") : 0;
        }
    }
}

}
