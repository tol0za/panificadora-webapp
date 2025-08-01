package dao;
import conexion.Conexion;
import modelo.NotaVenta;
import modelo.DetalleNotaVenta;
import modelo.Repartidor;
import modelo.Tienda;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class NotaVentaDAO {
    private Connection con;

    public NotaVentaDAO() throws SQLException {
        con = Conexion.getConnection();
    }

    // Insertar nueva nota de venta, devuelve el id generado
    public int insertarNotaVenta(NotaVenta nota) throws SQLException {
        String sql = "INSERT INTO notas_venta (id_repartidor, id_tienda, fecha_nota, total) VALUES (?, ?, NOW(), ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, nota.getIdRepartidor());
            stmt.setInt(2, nota.getIdTienda());
            stmt.setBigDecimal(3, nota.getTotal());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id de la nueva nota de venta");
    }

    // Insertar detalles de la nota de venta
    public void insertarDetalleNotaVenta(List<DetalleNotaVenta> detalles, int idNotaVenta) throws SQLException {
        String sql = "INSERT INTO detalle_nota_venta (id_nota, id_empaque, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (DetalleNotaVenta det : detalles) {
                stmt.setInt(1, idNotaVenta);
                stmt.setInt(2, det.getIdEmpaque());
                stmt.setInt(3, det.getCantidad());
                stmt.setBigDecimal(4, det.getPrecioUnitario());
                stmt.setBigDecimal(5, det.getSubtotal());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // Listar notas de venta de un día
    public List<NotaVenta> listarNotasPorDia(java.sql.Date fecha) throws SQLException {
        List<NotaVenta> lista = new ArrayList<>();
        String sql = "SELECT nv.*, r.nombre_repartidor, r.apellido_repartidor, t.nombre_tienda " +
                     "FROM notas_venta nv " +
                     "INNER JOIN repartidores r ON nv.id_repartidor = r.id_repartidor " +
                     "INNER JOIN tiendas t ON nv.id_tienda = t.id_tienda " +
                     "WHERE DATE(nv.fecha_nota) = ? ORDER BY nv.fecha_nota DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDate(1, fecha);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NotaVenta nv = new NotaVenta();
                    nv.setIdNota(rs.getInt("id_nota"));
                    nv.setIdRepartidor(rs.getInt("id_repartidor"));
                    nv.setIdTienda(rs.getInt("id_tienda"));
                    nv.setFecha(rs.getDate("fecha_nota"));
                    nv.setTotal(rs.getBigDecimal("total"));
                    nv.setNombreRepartidor(rs.getString("nombre_repartidor"));
                    nv.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                    nv.setNombreTienda(rs.getString("nombre_tienda"));
                    lista.add(nv);
                }
            }
        }
        return lista;
    }

    // Calcular total de ventas del día
    public BigDecimal totalNotasDelDia(java.sql.Date fecha) throws SQLException {
        String sql = "SELECT SUM(total) as total FROM notas_venta WHERE DATE(fecha_nota) = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDate(1, fecha);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total") != null ? rs.getBigDecimal("total") : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    // Eliminar una nota de venta
    public void eliminarNotaVenta(int idNota) throws SQLException {
        String sql = "DELETE FROM notas_venta WHERE id_nota = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            stmt.executeUpdate();
        }
    }

    // Eliminar detalles de una nota
    public void eliminarDetalleNotaVentaPorNota(int idNota) throws SQLException {
        String sql = "DELETE FROM detalle_nota_venta WHERE id_nota = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            stmt.executeUpdate();
        }
    }

    // Obtener una nota de venta por su id
    public NotaVenta obtenerNotaPorId(int idNota) throws SQLException {
        String sql = "SELECT nv.*, r.nombre_repartidor, r.apellido_repartidor, t.nombre_tienda " +
                     "FROM notas_venta nv " +
                     "INNER JOIN repartidores r ON nv.id_repartidor = r.id_repartidor " +
                     "INNER JOIN tiendas t ON nv.id_tienda = t.id_tienda " +
                     "WHERE nv.id_nota = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    NotaVenta nv = new NotaVenta();
                    nv.setIdNota(rs.getInt("id_nota"));
                    nv.setIdRepartidor(rs.getInt("id_repartidor"));
                    nv.setIdTienda(rs.getInt("id_tienda"));
                    nv.setFecha(rs.getDate("fecha_nota"));
                    nv.setTotal(rs.getBigDecimal("total"));
                    nv.setNombreRepartidor(rs.getString("nombre_repartidor"));
                    nv.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                    nv.setNombreTienda(rs.getString("nombre_tienda"));
                    return nv;
                }
            }
        }
        return null;
    }

    // Listar detalles por nota
    public List<DetalleNotaVenta> listarDetallesPorNota(int idNota) throws SQLException {
        List<DetalleNotaVenta> lista = new ArrayList<>();
        String sql = "SELECT dnv.*, e.nombre_empaque " +
                     "FROM detalle_nota_venta dnv " +
                     "INNER JOIN catalogo_empaque e ON dnv.id_empaque = e.id_empaque " +
                     "WHERE dnv.id_nota = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DetalleNotaVenta det = new DetalleNotaVenta();
                    det.setIdDetalle(rs.getInt("id_detalle"));
                    det.setIdNota(rs.getInt("id_nota"));
                    det.setIdEmpaque(rs.getInt("id_empaque"));
                    det.setCantidad(rs.getInt("cantidad"));
                    det.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    det.setSubtotal(rs.getBigDecimal("subtotal"));
                    det.setNombreEmpaque(rs.getString("nombre_empaque"));
                    lista.add(det);
                }
            }
        }
        return lista;
    }

    // Actualizar nota de venta (total, repartidor, tienda)
    public void actualizarNotaVenta(NotaVenta nota) throws SQLException {
        String sql = "UPDATE notas_venta SET id_repartidor = ?, id_tienda = ?, total = ? WHERE id_nota = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, nota.getIdRepartidor());
            stmt.setInt(2, nota.getIdTienda());
            stmt.setBigDecimal(3, nota.getTotal());
            stmt.setInt(4, nota.getIdNota());
            stmt.executeUpdate();
        }
    }
}
