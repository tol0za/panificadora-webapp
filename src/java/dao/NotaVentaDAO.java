package dao;

import conexion.Conexion;
import modelo.NotaVenta;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NotaVentaDAO {

    /* ========================= Helpers ========================= */
    private Connection getConn() throws SQLException {
        return Conexion.getConnection();
    }

    private NotaVenta mapRow(ResultSet rs) throws SQLException {
        NotaVenta n = new NotaVenta();
        n.setIdNotaVenta(rs.getInt("id_nota"));
        n.setFolio(rs.getInt("folio"));
        n.setIdRepartidor(rs.getInt("id_repartidor"));
        n.setIdTienda(rs.getInt("id_tienda"));
        Timestamp ts = rs.getTimestamp("fecha_nota");
        n.setFechaNota(ts != null ? ts.toLocalDateTime() : null);
        n.setTotal(rs.getDouble("total"));
        return n;
    }

    /* ========================= CRUD ========================= */

    /** Inserta cabecera de nota en notas_venta y devuelve el id_nota generado. */
    public int insertar(NotaVenta n) throws SQLException {
        final String sql = """
            INSERT INTO notas_venta (folio, id_repartidor, id_tienda, fecha_nota, total)
            VALUES (?, ?, ?, ?, 0)
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, n.getFolio());
            ps.setInt(2, n.getIdRepartidor());
            ps.setInt(3, n.getIdTienda());
            LocalDateTime f = n.getFechaNota() != null ? n.getFechaNota() : LocalDateTime.now();
            ps.setTimestamp(4, Timestamp.valueOf(f));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener id_nota generado");
    }

    /** Actualiza folio, tienda y total (fecha opcional si viene seteada). */
    public void actualizar(NotaVenta n) throws SQLException {
        boolean withFecha = (n.getFechaNota() != null);
        final String sql = withFecha
                ? """
                   UPDATE notas_venta
                      SET folio = ?, id_tienda = ?, total = ?, fecha_nota = ?
                    WHERE id_nota = ?
                  """
                : """
                   UPDATE notas_venta
                      SET folio = ?, id_tienda = ?, total = ?
                    WHERE id_nota = ?
                  """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, n.getFolio());
            ps.setInt(2, n.getIdTienda());
            ps.setDouble(3, n.getTotal());
            if (withFecha) {
                ps.setTimestamp(4, Timestamp.valueOf(n.getFechaNota()));
                ps.setInt(5, n.getIdNotaVenta());
            } else {
                ps.setInt(4, n.getIdNotaVenta());
            }
            ps.executeUpdate();
        }
    }

    /** Elimina la cabecera (detalle se borra por FK ON DELETE CASCADE). */
    public void eliminar(int idNota) throws SQLException {
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement("DELETE FROM notas_venta WHERE id_nota = ?")) {
            ps.setInt(1, idNota);
            ps.executeUpdate();
        }
    }

    /** Obtiene una nota por id. */
    public NotaVenta obtener(int idNota) throws SQLException {
        final String sql = "SELECT * FROM notas_venta WHERE id_nota = ?";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /* ========================= Consultas de apoyo ========================= */

    /** Verifica si existe folio. */
    public boolean folioExiste(int folio) throws SQLException {
        final String sql = "SELECT 1 FROM notas_venta WHERE folio = ? LIMIT 1";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Lista notas de un repartidor en una fecha (sin total calculado). */
    public List<NotaVenta> listarPorRepartidorYFecha(int idRep, LocalDate fecha) throws SQLException {
        final String sql = """
            SELECT * FROM notas_venta
             WHERE id_repartidor = ?
               AND DATE(fecha_nota) = ?
             ORDER BY fecha_nota DESC, id_nota DESC
        """;
        List<NotaVenta> out = new ArrayList<>();
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }
        return out;
    }

    /** Cuenta cuántas notas tiene un repartidor en una fecha. */
    public int contarPorRepartidorYFecha(int idRep, LocalDate fecha) throws SQLException {
        final String sql = """
            SELECT COUNT(*) FROM notas_venta
             WHERE id_repartidor = ?
               AND DATE(fecha_nota) = ?
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Total del día (neto) para un repartidor.
     * Suma (vendidas - merma) * precio_unitario sobre el detalle del día.
     */
    public double getTotalDia(int idRep, LocalDate fecha) throws SQLException {
        final String sql = """
            SELECT COALESCE(SUM( (d.cantidad_vendida - COALESCE(d.merma,0)) * d.precio_unitario ), 0)
              FROM notas_venta n
              JOIN detalle_nota_venta d ON d.id_nota = n.id_nota
             WHERE n.id_repartidor = ?
               AND DATE(n.fecha_nota) = ?
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    /**
     * Recalcula y persiste el total neto de una nota en notas_venta.total
     * (evita el 500 por tablas mal nombradas).
     */
    public void actualizarTotal(int idNota) throws SQLException {
        final String sql = """
            UPDATE notas_venta nv
               SET nv.total = (
                 SELECT COALESCE(SUM( (d.cantidad_vendida - COALESCE(d.merma,0)) * d.precio_unitario ), 0)
                   FROM detalle_nota_venta d
                  WHERE d.id_nota = nv.id_nota
               )
             WHERE nv.id_nota = ?
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            ps.executeUpdate();
        }
    }

    /* ========================= Historial / reportes ========================= */

    /** Lista notas por rango de fechas (se pueden ordenar/mostrar en JSP). */
    public List<NotaVenta> listarPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        final String sql = """
            SELECT * FROM notas_venta
             WHERE DATE(fecha_nota) BETWEEN ? AND ?
             ORDER BY fecha_nota DESC, id_nota DESC
        """;
        List<NotaVenta> out = new ArrayList<>();
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }
        return out;
    }

    /** Resumen diario: fecha -> total neto en ese día. */
    public List<Map<String, Object>> resumenDiario(LocalDate desde, LocalDate hasta) throws SQLException {
        final String sql = """
            SELECT DATE(n.fecha_nota) AS fecha,
                   COALESCE(SUM( (d.cantidad_vendida - COALESCE(d.merma,0)) * d.precio_unitario ), 0) AS total
              FROM notas_venta n
              LEFT JOIN detalle_nota_venta d ON d.id_nota = n.id_nota
             WHERE DATE(n.fecha_nota) BETWEEN ? AND ?
             GROUP BY DATE(n.fecha_nota)
             ORDER BY DATE(n.fecha_nota)
        """;
        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("fecha", rs.getDate("fecha").toLocalDate());
                    row.put("total", rs.getDouble("total"));
                    out.add(row);
                }
            }
        }
        return out;
    }

    /** Resumen por repartidor: id_repartidor -> total neto en el rango. */
    public List<Map<String, Object>> resumenPorRepartidor(LocalDate desde, LocalDate hasta) throws SQLException {
        final String sql = """
            SELECT n.id_repartidor,
                   COALESCE(SUM( (d.cantidad_vendida - COALESCE(d.merma,0)) * d.precio_unitario ), 0) AS total
              FROM notas_venta n
              LEFT JOIN detalle_nota_venta d ON d.id_nota = n.id_nota
             WHERE DATE(n.fecha_nota) BETWEEN ? AND ?
             GROUP BY n.id_repartidor
             ORDER BY total DESC
        """;
        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id_repartidor", rs.getInt("id_repartidor"));
                    row.put("total", rs.getDouble("total"));
                    out.add(row);
                }
            }
        }
        return out;
    }
}