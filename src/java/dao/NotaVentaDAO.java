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
 * Mantiene métodos históricos y agrega consultas de historial por fechas.
 */
public class NotaVentaDAO {

    /* =====================  utilidades  ===================== */
    private Connection getConn() throws SQLException {
        return Conexion.getConnection();
    }

    /* ===================== CRUD básicos ===================== */

    /** Verifica si un folio ya existe. */
    public boolean folioExiste(int folio) throws SQLException {
        String sql = "SELECT 1 FROM notas_venta WHERE folio = ? LIMIT 1";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserta una nota de venta y retorna el id generado. */
    public int insertar(NotaVenta n) throws SQLException {
        String sql = "INSERT INTO notas_venta (folio, id_repartidor, id_tienda, fecha_nota, total) VALUES (?,?,?,?,?)";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    /** Actualiza encabezado (folio, tienda). No toca el total; usa actualizarTotal(). */
    public void actualizar(NotaVenta n) throws SQLException {
        String sql = "UPDATE notas_venta SET folio=?, id_tienda=? WHERE id_nota=?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, n.getFolio());
            ps.setInt(2, n.getIdTienda());
            ps.setInt(3, n.getIdNotaVenta());
            ps.executeUpdate();
        }
    }

    /** Borra la nota (el detalle se elimina en cascada si la FK está ON DELETE CASCADE). */
    public void eliminar(int idNota) throws SQLException {
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement("DELETE FROM notas_venta WHERE id_nota=?")) {
            ps.setInt(1, idNota);
            ps.executeUpdate();
        }
    }

    /** Recalcula y actualiza el total de la nota con la suma de sus detalles. */
    public void actualizarTotal(int idNota) throws SQLException {
        String sql = """
            UPDATE notas_venta n
            JOIN (
              SELECT id_nota, COALESCE(SUM(total_linea),0) AS t
                FROM detalle_nota_venta
               WHERE id_nota = ?
               GROUP BY id_nota
            ) x ON x.id_nota = n.id_nota
               SET n.total = x.t
             WHERE n.id_nota = ?
        """;
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            ps.setInt(2, idNota);
            ps.executeUpdate();
        }
    }

    /** Devuelve una nota por id. */
    public NotaVenta obtener(int idNota) throws SQLException {
        String sql = "SELECT * FROM notas_venta WHERE id_nota = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Lista todas las notas de un repartidor en una fecha concreta. */
    public List<NotaVenta> listarPorRepartidorYFecha(int idRepartidor, LocalDate fecha) throws SQLException {
        List<NotaVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM notas_venta WHERE id_repartidor = ? AND DATE(fecha_nota) = ? ORDER BY id_nota";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    /** Suma total de un repartidor en una fecha (solo vendidas). */
    public double getTotalDia(int idRepartidor, LocalDate fecha) throws SQLException {
        String sql = "SELECT IFNULL(SUM(total),0) FROM notas_venta WHERE id_repartidor=? AND DATE(fecha_nota)=?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /* ===================== HISTORIAL POR FECHAS (NUEVO) ===================== */

    /** Fila para listado general del historial (con getters para EL/JSTL). */
    public static class NotaVentaRow {
        private int idNota;
        private String folio;
        private String repartidor;
        private String tienda;
        private double total;
        private Timestamp fechaHora;

        public int getIdNota() { return idNota; }
        public void setIdNota(int idNota) { this.idNota = idNota; }
        public String getFolio() { return folio; }
        public void setFolio(String folio) { this.folio = folio; }
        public String getRepartidor() { return repartidor; }
        public void setRepartidor(String repartidor) { this.repartidor = repartidor; }
        public String getTienda() { return tienda; }
        public void setTienda(String tienda) { this.tienda = tienda; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        public Timestamp getFechaHora() { return fechaHora; }
        public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }
    }

    /** Resumen por día (para gráficas) – con getters. */
    public static class VentaDiaria {
        private LocalDate fecha;
        private double totalDia;
        private int notas;

        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public double getTotalDia() { return totalDia; }
        public void setTotalDia(double totalDia) { this.totalDia = totalDia; }
        public int getNotas() { return notas; }
        public void setNotas(int notas) { this.notas = notas; }
    }

    /** Resumen por repartidor – con getters. */
    public static class VentaRepartidor {
        private int idRepartidor;
        private String repartidor;
        private double total;
        private int notas;

        public int getIdRepartidor() { return idRepartidor; }
        public void setIdRepartidor(int idRepartidor) { this.idRepartidor = idRepartidor; }
        public String getRepartidor() { return repartidor; }
        public void setRepartidor(String repartidor) { this.repartidor = repartidor; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        public int getNotas() { return notas; }
        public void setNotas(int notas) { this.notas = notas; }
    }

    /** Lista de notas entre [desde, hasta] (incluye joins para nombres). */
    public List<NotaVentaRow> listarPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
            SELECT nv.id_nota      AS id_nota_venta,
                   nv.folio,
                   TRIM(CONCAT(r.nombre_repartidor,' ',IFNULL(r.apellido_repartidor,''))) AS repartidor,
                   COALESCE(t.nombre_tienda, CONCAT('ID ', nv.id_tienda)) AS tienda,
                   nv.total,
                   nv.fecha_nota
              FROM notas_venta nv
              LEFT JOIN repartidores r ON r.id_repartidor = nv.id_repartidor
              LEFT JOIN tiendas      t ON t.id_tienda     = nv.id_tienda
             WHERE nv.fecha_nota >= ? AND nv.fecha_nota < ?
             ORDER BY nv.fecha_nota DESC, nv.id_nota DESC
        """;
        List<NotaVentaRow> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NotaVentaRow r = new NotaVentaRow();
                    r.setIdNota(rs.getInt("id_nota_venta"));
                    r.setFolio(rs.getString("folio"));
                    r.setRepartidor(rs.getString("repartidor"));
                    r.setTienda(rs.getString("tienda"));       // nombre_tienda
                    r.setTotal(rs.getDouble("total"));
                    r.setFechaHora(rs.getTimestamp("fecha_nota"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    /** Resumen por día: SUM(total) y conteo de notas. */
    public List<VentaDiaria> resumenDiario(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
            SELECT DATE(nv.fecha_nota) AS f, SUM(nv.total) AS total_dia, COUNT(*) AS notas
              FROM notas_venta nv
             WHERE nv.fecha_nota >= ? AND nv.fecha_nota < ?
             GROUP BY DATE(nv.fecha_nota)
             ORDER BY f
        """;
        List<VentaDiaria> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaDiaria v = new VentaDiaria();
                    v.setFecha(rs.getDate("f").toLocalDate());
                    v.setTotalDia(rs.getDouble("total_dia"));
                    v.setNotas(rs.getInt("notas"));
                    out.add(v);
                }
            }
        }
        return out;
    }

    /** Resumen por repartidor: SUM(total) y conteo. */
    public List<VentaRepartidor> resumenPorRepartidor(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
            SELECT nv.id_repartidor,
                   TRIM(CONCAT(r.nombre_repartidor,' ',IFNULL(r.apellido_repartidor,''))) AS repartidor,
                   SUM(nv.total) AS total_rep,
                   COUNT(*) AS notas
              FROM notas_venta nv
              LEFT JOIN repartidores r ON r.id_repartidor = nv.id_repartidor
             WHERE nv.fecha_nota >= ? AND nv.fecha_nota < ?
             GROUP BY nv.id_repartidor, r.nombre_repartidor, r.apellido_repartidor
             ORDER BY total_rep DESC
        """;
        List<VentaRepartidor> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaRepartidor v = new VentaRepartidor();
                    v.setIdRepartidor(rs.getInt("id_repartidor"));
                    v.setRepartidor(rs.getString("repartidor"));
                    v.setTotal(rs.getDouble("total_rep"));
                    v.setNotas(rs.getInt("notas"));
                    out.add(v);
                }
            }
        }
        return out;
    }

    /* ===================== mapeo base ===================== */
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
    /** Número de notas de un repartidor en una fecha (hoy). */
public int contarPorRepartidorYFecha(int idRepartidor, LocalDate fecha) throws SQLException {
    String sql = "SELECT COUNT(*) FROM notas_venta WHERE id_repartidor=? AND DATE(fecha_nota)=?";
    try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, idRepartidor);
        ps.setDate(2, java.sql.Date.valueOf(fecha));
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
}
