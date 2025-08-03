package dao;

import conexion.Conexion;
import modelo.DetalleNotaVenta;
import modelo.NotaVenta;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotaVentaDAO {
    private Connection conn;

    public NotaVentaDAO() throws SQLException {
        conn = Conexion.getConnection();
    }

    public boolean insertarNotaVenta(NotaVenta nota) throws SQLException {
        String sqlNota = "INSERT INTO notas_venta (folio, fecha_nota, id_repartidor, id_tienda, total) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_nota_venta (id_nota, id_empaque, cantidad_vendida, precio_unitario, merma) VALUES (?, ?, ?, ?, ?)";
        try (
            PreparedStatement psNota = conn.prepareStatement(sqlNota, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement psDetalle = conn.prepareStatement(sqlDetalle)
        ) {
            conn.setAutoCommit(false);
            psNota.setString(1, nota.getFolio());
            psNota.setTimestamp(2, Timestamp.valueOf(nota.getFecha()));
            psNota.setInt(3, nota.getIdRepartidor());
            psNota.setInt(4, nota.getIdTienda());
            psNota.setDouble(5, nota.getTotalNota());
            psNota.executeUpdate();

            ResultSet rs = psNota.getGeneratedKeys();
            if (rs.next()) {
                int idNotaVenta = rs.getInt(1);
                nota.setIdNotaVenta(idNotaVenta);
                for (DetalleNotaVenta det : nota.getDetalles()) {
                    psDetalle.setInt(1, idNotaVenta);
                    psDetalle.setInt(2, det.getIdEmpaque());
                    psDetalle.setInt(3, det.getCantidadVendida());
                    psDetalle.setDouble(4, det.getPrecioUnitario());
                    psDetalle.setInt(5, det.getMerma());
                    psDetalle.addBatch();
                }
                psDetalle.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<NotaVenta> listarTodas() throws SQLException {
        List<NotaVenta> notas = new ArrayList<>();
        String sql = "SELECT nv.id_nota, nv.folio, nv.fecha_nota, nv.id_repartidor, r.nombre_repartidor, " +
                     "nv.id_tienda, t.nombre_tienda, nv.total " +
                     "FROM notas_venta nv " +
                     "JOIN repartidores r ON nv.id_repartidor = r.id_repartidor " +
                     "JOIN tiendas t ON nv.id_tienda = t.id_tienda " +
                     "ORDER BY nv.fecha_nota DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                NotaVenta nota = new NotaVenta();
                nota.setIdNotaVenta(rs.getInt("id_nota"));
                nota.setFolio(rs.getString("folio"));
                nota.setFecha(rs.getTimestamp("fecha_nota").toLocalDateTime());
                nota.setIdRepartidor(rs.getInt("id_repartidor"));
                nota.setNombreRepartidor(rs.getString("nombre_repartidor"));
                nota.setIdTienda(rs.getInt("id_tienda"));
                nota.setNombreTienda(rs.getString("nombre_tienda"));
                nota.setTotalNota(rs.getDouble("total"));
                notas.add(nota);
            }
        }
        return notas;
    }

    public NotaVenta buscarPorId(int idNota) throws SQLException {
        NotaVenta nota = null;
        String sql = "SELECT nv.id_nota, nv.folio, nv.fecha_nota, nv.id_repartidor, r.nombre_repartidor, " +
                     "nv.id_tienda, t.nombre_tienda, nv.total " +
                     "FROM notas_venta nv " +
                     "JOIN repartidores r ON nv.id_repartidor = r.id_repartidor " +
                     "JOIN tiendas t ON nv.id_tienda = t.id_tienda " +
                     "WHERE nv.id_nota = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nota = new NotaVenta();
                    nota.setIdNotaVenta(rs.getInt("id_nota"));
                    nota.setFolio(rs.getString("folio"));
                    nota.setFecha(rs.getTimestamp("fecha_nota").toLocalDateTime());
                    nota.setIdRepartidor(rs.getInt("id_repartidor"));
                    nota.setNombreRepartidor(rs.getString("nombre_repartidor"));
                    nota.setIdTienda(rs.getInt("id_tienda"));
                    nota.setNombreTienda(rs.getString("nombre_tienda"));
                    nota.setTotalNota(rs.getDouble("total"));

                    // Obtener detalles
                    DetalleNotaVentaDAO detalleDAO = new DetalleNotaVentaDAO();
                    List<DetalleNotaVenta> detalles = detalleDAO.buscarPorNota(idNota);
                    nota.setDetalles(detalles);
                }
            }
        }
        return nota;
    }
}