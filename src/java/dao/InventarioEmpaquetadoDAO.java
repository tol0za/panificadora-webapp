package dao;

import conexion.Conexion;
import modelo.InventarioEmpaquetado;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

/**
 * DAO para la tabla <code>inventario_empaquetado</code>.
 * 
 * ► Se conservan TODOS los métodos que ya tenías para no romper módulos previos.
 * ► Se añaden <code>registrarMovimientoSalida</code> y <code>regresarStockGeneral</code>
 *    requeridos por el nuevo módulo de Notas de Venta.
 */
public class InventarioEmpaquetadoDAO {

    private final Connection con;

    public InventarioEmpaquetadoDAO() throws SQLException {
        this.con = Conexion.getConnection();
    }

    /* ===================================================== */
    /*  MÉTODOS EXISTENTES                                   */
    /* ===================================================== */

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
                Timestamp ts = rs.getTimestamp("fecha");
                if (ts != null) {
                    ie.setFecha(ts.toLocalDateTime());
                    ie.setFechaDate(Date.from(ie.getFecha().atZone(ZoneId.systemDefault()).toInstant()));
                }
                lista.add(ie);
            }
        }
        return lista;
    }

    public void registrarMovimiento(InventarioEmpaquetado mov) throws SQLException {
        String sql = "INSERT INTO inventario_empaquetado (id_empaque, cantidad, fecha, motivo, cantidad_actual) " +
                     "VALUES (?, ?, ?, ?, ?)";
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, mov.getIdEmpaque());
            ps.setInt(2, mov.getCantidad());
            ps.setTimestamp(3, Timestamp.valueOf(mov.getFecha()));
            ps.setString(4, mov.getMotivo());
            ps.setInt(5, mov.getCantidadActual());
            ps.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    public int obtenerCantidadActual(int idEmpaque) throws SQLException {
        String sql = "SELECT cantidad_actual FROM inventario_empaquetado WHERE id_empaque = ? ORDER BY fecha DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpaque);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cantidad_actual") : 0;
            }
        }
    }

    public BigDecimal obtenerPrecioUnitario(int idEmpaque) throws SQLException {
        String sql = "SELECT precio_unitario FROM catalogo_empaque WHERE id_empaque = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpaque);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("precio_unitario");
            }
        }
        return BigDecimal.ZERO;
    }

    public void ajustarInventario(int idEmpaque, int delta, String motivo) throws SQLException {
        int actual = obtenerCantidadActual(idEmpaque);
        int nuevo = actual + delta;
        String sql = "INSERT INTO inventario_empaquetado (id_empaque, cantidad, fecha, motivo, cantidad_actual) " +
                     "VALUES (?, ?, NOW(), ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpaque);
            ps.setInt(2, delta);
            ps.setString(3, motivo);
            ps.setInt(4, nuevo);
            ps.executeUpdate();
        }
    }

    public int obtenerStockActual(int idEmpaque) throws SQLException {
        String sql = "SELECT cantidad_actual FROM inventario_empaquetado WHERE id_empaque = ? ORDER BY fecha DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpaque);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cantidad_actual") : 0;
            }
        }
        
    }

    public int obtenerCantidadActualPorRepartidorYEmpaque(int idRepartidor, int idEmpaque) throws SQLException {
        String sql = "SELECT cantidad_actual FROM inventario_empaquetado " +
                     "WHERE id_repartidor = ? AND id_empaque = ? " +
                     "ORDER BY fecha DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setInt(2, idEmpaque);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cantidad_actual");
            }
        }
        return 0;
    }

    public List<InventarioEmpaquetado> obtenerPorDistribucion(int idDistribucion) throws SQLException {
        List<InventarioEmpaquetado> lista = new ArrayList<>();
        String sql = "SELECT * FROM inventario_empaquetado WHERE id_distribucion = ? ORDER BY id_empaque";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDistribucion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventarioEmpaquetado inv = new InventarioEmpaquetado();
                    inv.setIdInventario(rs.getInt("id_inventario"));
                    inv.setIdDistribucion(rs.getInt("id_distribucion"));
                    inv.setIdEmpaque(rs.getInt("id_empaque"));
                    inv.setCantidadActual(rs.getInt("cantidad_actual"));
                    lista.add(inv);
                }
            }
        }
        return lista;
    }

    public void actualizarStockDespuesVenta(int idDistribucion, int idEmpaque, int cantidadVendida, int merma) throws SQLException {
        String sql = "UPDATE inventario_empaquetado SET cantidad_actual = cantidad_actual - ? WHERE id_distribucion = ? AND id_empaque = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int total = cantidadVendida + merma;
            ps.setInt(1, total);
            ps.setInt(2, idDistribucion);
            ps.setInt(3, idEmpaque);
            ps.executeUpdate();
        }
    }

    /* ===================================================== */
    /*  MÉTODOS NUEVOS – módulo Notas de Venta               */
    /* ===================================================== */

    /**
     * Registra un movimiento de salida (venta + merma) enlazado a la distribución.
     */
    public void registrarMovimientoSalida(int idEmpaque, int cantidad, int idDistribucion, int idRepartidor) throws SQLException {
        String sql = "INSERT INTO inventario_empaquetado " +
                     "(id_empaque, cantidad, tipo_movimiento, id_distribucion, id_repartidor, fecha, cantidad_actual) " +
                     "VALUES (?, ?, 'SALIDA_VENTA', ?, ?, NOW(), ?)";
        // calculamos nuevo stock actual basado en el último registro
        int nuevoActual = obtenerCantidadActual(idEmpaque) - cantidad;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpaque);
            ps.setInt(2, cantidad);
            ps.setInt(3, idDistribucion);
            ps.setInt(4, idRepartidor);
            ps.setInt(5, nuevoActual);
            ps.executeUpdate();
        }
    }

    /**
     * Devuelve piezas no vendidas al inventario global (entrada retorno).
     */
    public void regresarStockGeneral(int idEmpaque, int cantidad) throws SQLException {
        String sql = "INSERT INTO inventario_empaquetado " +
                     "(id_empaque, cantidad, tipo_movimiento, fecha, cantidad_actual) " +
                     "VALUES (?, ?, 'ENTRADA_RETORNO', NOW(), ?)";
        int nuevoActual = obtenerCantidadActual(idEmpaque) + cantidad;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpaque);
            ps.setInt(2, cantidad);
            ps.setInt(3, nuevoActual);
            ps.executeUpdate();
        }
    }
}
