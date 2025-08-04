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
        try (Connection c = Conexion.getConnection()) {
            insertarInterno(c, List.of(d));     // reutiliza el lote
        }
    }

    /* NUEVO — misma lógica pero recibe varias líneas a la vez       *
     *       — no rompe quienes aún llamen insertar(DetalleNotaVenta) */
    public void insertar(List<DetalleNotaVenta> lista) throws SQLException {
        if (lista == null || lista.isEmpty()) return;
        try (Connection c = Conexion.getConnection()) {
            insertarInterno(c, lista);
        }
    }
    
      private void insertarInterno(Connection c, List<DetalleNotaVenta> lista) throws SQLException {
        String sql = "INSERT INTO detalle_nota_venta "
                   + "(id_nota,id_distribucion,id_empaque,cantidad_vendida,merma,precio_unitario)"
                   + "VALUES (?,?,?,?,?,?)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (DetalleNotaVenta d : lista) {
                ps.setInt   (1, d.getIdNota());
                ps.setInt   (2, d.getIdDistribucion());
                ps.setInt   (3, d.getIdEmpaque());
                ps.setInt   (4, d.getCantidadVendida());
                ps.setInt   (5, d.getMerma());
                ps.setDouble(6, d.getPrecioUnitario());
                ps.addBatch();
            }
            ps.executeBatch();          // 🚀 un solo round-trip
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
    String sql = """
        SELECT d.*, e.nombre_empaque, d.precio_unitario, d.total_linea
        FROM detalle_nota_venta d
        JOIN catalogo_empaque e ON e.id_empaque = d.id_empaque
        WHERE d.id_nota = ?
        ORDER BY d.id_detalle
    """;
    List<DetalleNotaVenta> list = new ArrayList<>();
    try (Connection c = Conexion.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, idNota);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DetalleNotaVenta d = new DetalleNotaVenta();
                d.setIdDetalle(      rs.getInt("id_detalle"));
                d.setIdNota(         rs.getInt("id_nota"));
                d.setIdDistribucion( rs.getInt("id_distribucion"));
                d.setIdEmpaque(      rs.getInt("id_empaque"));
                d.setCantidadVendida(rs.getInt("cantidad_vendida"));
                d.setMerma(          rs.getInt("merma"));
                d.setPrecioUnitario( rs.getDouble("precio_unitario"));
                d.setTotalLinea(     rs.getDouble("total_linea"));
                d.setNombreEmpaque(  rs.getString("nombre_empaque"));
                list.add(d);
            }
        }
    }
    return list;
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
