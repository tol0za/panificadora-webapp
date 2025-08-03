package dao;

import conexion.Conexion;
import modelo.NotaVenta;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO dedicado al encabezado de Notas de Venta.
 *
 * ↪ 100 % NUEVO → no colisiona con otros módulos.
 */
public class NotaVentaDAO {

    /* =====================  utilidades  ===================== */
    private Connection getConn() throws SQLException {
        return Conexion.getConnection();
    }

    /* ======================================================== */

    /**
     * Verifica si un folio ya existe.
     */
    public boolean folioExiste(int folio) throws SQLException {
        String sql = "SELECT 1 FROM notas_venta WHERE folio = ? LIMIT 1";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Inserta una nota de venta y retorna el id generado.
     */
    public int insertar(NotaVenta n) throws SQLException {
        String sql = "INSERT INTO notas_venta (folio, id_repartidor, id_tienda, fecha_nota, total) " +
                     "VALUES (?,?,?,?,?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getFolio());
            ps.setInt(2, n.getIdRepartidor());
            ps.setInt(3, n.getIdTienda());
            ps.setTimestamp(4, Timestamp.valueOf(n.getFechaNota() == null ? LocalDateTime.now() : n.getFechaNota()));
            ps.setDouble(5, n.getTotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Actualiza encabezado (folio, tienda). No toca el total; usa actualizarTotal().
     */
    public void actualizar(NotaVenta n) throws SQLException {
        String sql = "UPDATE notas_venta SET folio=?, id_tienda=? WHERE id_nota=?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, n.getFolio());
            ps.setInt(2, n.getIdTienda());
            ps.setInt(3, n.getIdNotaVenta());
            ps.executeUpdate();
        }
    }

    /**
     * Borra la nota (el detalle se elimina en cascada si la FK está ON DELETE CASCADE).
     */
    public void eliminar(int idNota) throws SQLException {
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement("DELETE FROM notas_venta WHERE id_nota=?")) {
            ps.setInt(1, idNota);
            ps.executeUpdate();
        }
    }

    /**
     * Recalcula y actualiza el total de la nota con la suma de sus detalles.
     */
    public void actualizarTotal(int idNota) throws SQLException {
        String sql = "UPDATE notas_venta nv SET nv.total = (\n" +
                     "  SELECT IFNULL(SUM(dnv.cantidad_vendida * dnv.precio_unitario),0)\n" +
                     "  FROM detalle_nota_venta dnv WHERE dnv.id_nota = nv.id_nota\n" +
                     ") WHERE nv.id_nota = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            ps.executeUpdate();
        }
    }

    /**
     * Devuelve una nota con su total y referencias de nombres (tienda, etc.).
     */
    public NotaVenta obtener(int idNota) throws SQLException {
        String sql = "SELECT * FROM notas_venta WHERE id_nota = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NotaVenta n = mapRow(rs);
                    return n;
                }
            }
        }
        return null;
    }

    /**
     * Lista todas las notas de un repartidor en una fecha concreta.
     */
    public List<NotaVenta> listarPorRepartidorYFecha(int idRepartidor, LocalDate fecha) throws SQLException {
        List<NotaVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM notas_venta WHERE id_repartidor = ? AND DATE(fecha_nota) = ? ORDER BY id_nota";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Suma total de un repartidor en una fecha (solo vendidas).
     */
    public double getTotalDia(int idRepartidor, LocalDate fecha) throws SQLException {
        String sql = "SELECT IFNULL(SUM(total),0) FROM notas_venta WHERE id_repartidor=? AND DATE(fecha_nota)=?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /* ======================================================== */

    private NotaVenta mapRow(ResultSet rs) throws SQLException {
        NotaVenta n = new NotaVenta();
        n.setIdNotaVenta(rs.getInt("id_nota"));
        n.setFolio(rs.getInt("folio"));
        n.setIdRepartidor(rs.getInt("id_repartidor"));
        n.setIdTienda(rs.getInt("id_tienda"));
        n.setFechaNota(rs.getTimestamp("fecha_nota").toLocalDateTime());
        n.setTotal(rs.getDouble("total"));
        return n;
    }
}
