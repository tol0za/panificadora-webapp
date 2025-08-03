package dao;

import conexion.Conexion;
import dto.DistribucionResumen;
import dto.InventarioDTO;
import modelo.Distribucion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla <code>distribucion</code>.
 * Incluye los métodos históricos usados por otros módulos y los nuevos
 * requeridos por el módulo de Notas de Venta.
 */
public class DistribucionDAO {

    /* ----------------------------------------------------- */
    /*  Utilidad                                             */
    /* ----------------------------------------------------- */
    private Connection getConn() throws SQLException {
        return Conexion.getConnection();
    }

    /* ----------------------------------------------------- */
    /*  Métodos heredados (ya utilizados en tu sistema)      */
    /* ----------------------------------------------------- */

    /**
     * Devuelve TODAS las filas de un repartidor en una fecha concreta.
     */
    public List<Distribucion> buscarPorRepartidorYFecha(int idRepartidor, LocalDate fecha) throws SQLException {
        List<Distribucion> lista = new ArrayList<>();
        String sql = "SELECT * FROM distribucion WHERE id_repartidor = ? AND DATE(fecha_distribucion) = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
     * Obtiene la última salida registrada de un repartidor.
     */
    public Distribucion obtenerUltimaDistribucionPorRepartidor(int idRepartidor) throws SQLException {
        String sql = "SELECT * FROM distribucion WHERE id_repartidor = ? ORDER BY fecha_distribucion DESC LIMIT 1";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /* ----------------------------------------------------- */
    /*  Métodos NUEVOS para módulo Notas de Venta            */
    /* ----------------------------------------------------- */

    /**
     * Devuelve los repartidores que registraron al menos una salida en la fecha dada.
     */
    public List<DistribucionResumen> repartidoresConSalida(LocalDate fecha) throws SQLException {
        List<DistribucionResumen> lista = new ArrayList<>();
        String sql = "SELECT MIN(d.id_distribucion) AS id_distribucion, " +
                     "       r.id_repartidor, r.nombre_repartidor " +
                     "FROM distribucion d " +
                     "JOIN repartidores r ON r.id_repartidor = d.id_repartidor " +
                     "WHERE DATE(d.fecha_distribucion) = ? " +
                     "GROUP BY r.id_repartidor, r.nombre_repartidor";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DistribucionResumen(
                            rs.getInt("id_distribucion"),
                            rs.getInt("id_repartidor"),
                            rs.getString("nombre_repartidor")));
                }
            }
        }
        return lista;
    }

    /**
     * Obtiene el inventario pendiente (restante>0) de un repartidor en la fecha dada.
     */
    public List<InventarioDTO> inventarioPendiente(int idRepartidor, LocalDate fecha) throws SQLException {
        List<InventarioDTO> lista = new ArrayList<>();
        String sql = "SELECT d.id_distribucion, d.id_empaque, ce.nombre_empaque, ce.precio_unitario, " +
                     "       (d.cantidad - IFNULL(ir.cantidad_vendida,0) - IFNULL(ir.cantidad_mermada,0)) AS restante " +
                     "FROM distribucion d " +
                     "JOIN catalogo_empaque ce ON ce.id_empaque = d.id_empaque " +
                     "LEFT JOIN inventario_repartidor ir " +
                     "  ON ir.id_repartidor = d.id_repartidor AND ir.id_empaque = d.id_empaque " +
                     "WHERE d.id_repartidor = ? AND DATE(d.fecha_distribucion) = ? " +
                     "HAVING restante > 0";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new InventarioDTO(
                            rs.getInt("id_distribucion"),
                            rs.getInt("id_empaque"),
                            rs.getString("nombre_empaque"),
                            rs.getDouble("precio_unitario"),
                            rs.getInt("restante")));
                }
            }
        }
        return lista;
    }

    /* ----------------------------------------------------- */
    /*  Helper de mapeo                                      */
    /* ----------------------------------------------------- */
    private Distribucion mapRow(ResultSet rs) throws SQLException {
        Distribucion d = new Distribucion();
        d.setIdDistribucion(rs.getInt("id_distribucion"));
        d.setIdRepartidor(rs.getInt("id_repartidor"));
        d.setIdEmpaque(rs.getInt("id_empaque"));
        d.setCantidad(rs.getInt("cantidad"));
        Timestamp ts = rs.getTimestamp("fecha_distribucion");
        if (ts != null) d.setFechaDistribucion(ts.toLocalDateTime());
        return d;
    }
}
