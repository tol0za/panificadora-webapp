package dao;

import conexion.Conexion;
import modelo.NotaVenta;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NotaVentaDAO {

    private Connection getConn() throws SQLException { return Conexion.getConnection(); }

    /* ===== CRUD ===== */

    public boolean folioExiste(int folio) throws SQLException {
        String sql = "SELECT 1 FROM notas_venta WHERE folio=? LIMIT 1";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try(ResultSet rs=ps.executeQuery()){ return rs.next(); }
        }
    }

    public int insertar(NotaVenta n) throws SQLException {
        String sql = "INSERT INTO notas_venta (folio, id_repartidor, id_tienda, fecha_nota, total) VALUES (?,?,?,?,0)";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getFolio());
            ps.setInt(2, n.getIdRepartidor());
            ps.setInt(3, n.getIdTienda());
            ps.setTimestamp(4, Timestamp.valueOf(n.getFechaNota()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se pudo generar id para notas_venta");
    }

    public NotaVenta obtener(int idNota) throws SQLException {
        String sql = "SELECT id_nota, folio, id_repartidor, id_tienda, fecha_nota, total FROM notas_venta WHERE id_nota=?";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            try (ResultSet rs=ps.executeQuery()){
                if (rs.next()){
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
        }
        return null;
    }

    public void actualizar(NotaVenta n) throws SQLException {
        String sql = "UPDATE notas_venta SET folio=?, id_tienda=? WHERE id_nota=?";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1, n.getFolio());
            ps.setInt(2, n.getIdTienda());
            ps.setInt(3, n.getIdNotaVenta());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idNota) throws SQLException {
        try (Connection c=getConn();
             PreparedStatement ps=c.prepareStatement("DELETE FROM notas_venta WHERE id_nota=?")) {
            ps.setInt(1, idNota);
            ps.executeUpdate();
        }
    }

    /* ===== Consultas ===== */

    /** Listado del día por repartidor */
    public List<NotaVenta> listarPorRepartidorYFecha(int idRep, LocalDate fecha) throws SQLException {
        List<NotaVenta> list = new ArrayList<>();
        String sql =
            "SELECT nv.id_nota, nv.folio, nv.id_repartidor, nv.id_tienda, nv.fecha_nota, " +
            "       COALESCE(SUM((dv.cantidad_vendida - IFNULL(dv.merma,0))*dv.precio_unitario),0) AS total " +
            "FROM notas_venta nv " +
            "LEFT JOIN detalle_nota_venta dv ON dv.id_nota = nv.id_nota " +
            "WHERE nv.id_repartidor=? AND DATE(nv.fecha_nota)=? " +
            "GROUP BY nv.id_nota, nv.folio, nv.id_repartidor, nv.id_tienda, nv.fecha_nota";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()){
                    NotaVenta n = new NotaVenta();
                    n.setIdNotaVenta(rs.getInt("id_nota"));
                    n.setFolio(rs.getInt("folio"));
                    n.setIdRepartidor(rs.getInt("id_repartidor"));
                    n.setIdTienda(rs.getInt("id_tienda"));
                    n.setFechaNota(rs.getTimestamp("fecha_nota").toLocalDateTime());
                    n.setTotal(rs.getDouble("total"));
                    list.add(n);
                }
            }
        }
        return list;
    }

    public int contarPorRepartidorYFecha(int idRep, LocalDate fecha) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notas_venta WHERE id_repartidor=? AND DATE(fecha_nota)=?";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs=ps.executeQuery()) { return rs.next()? rs.getInt(1) : 0; }
        }
    }

    /** Total del día (cobradas) para badges */
    public double getTotalDia(int idRep, LocalDate fecha) throws SQLException {
        String sql =
            "SELECT COALESCE(SUM((dv.cantidad_vendida - IFNULL(dv.merma,0))*dv.precio_unitario),0) " +
            "FROM notas_venta nv JOIN detalle_nota_venta dv ON dv.id_nota = nv.id_nota " +
            "WHERE nv.id_repartidor=? AND DATE(nv.fecha_nota)=?";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs=ps.executeQuery()){ return rs.next()? rs.getDouble(1) : 0.0; }
        }
    }

    /** Recalcula y guarda el total de una nota (cobradas) */
    public void actualizarTotal(int idNota) throws SQLException {
        String sql =
            "UPDATE notas_venta nv SET total = (" +
            "  SELECT COALESCE(SUM((dv.cantidad_vendida - IFNULL(dv.merma,0))*dv.precio_unitario),0) " +
            "  FROM detalle_nota_venta dv WHERE dv.id_nota = ?" +
            ") WHERE nv.id_nota = ?";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            ps.setInt(2, idNota);
            ps.executeUpdate();
        }
    }

    /* ===== Historial ===== */

    public static class ResumenDia {
        private String fecha;
        private int notas;
        private double totalDia;
        public String getFecha(){ return fecha; }
        public int getNotas(){ return notas; }
        public double getTotalDia(){ return totalDia; }
        public void setFecha(String s){ this.fecha=s; }
        public void setNotas(int n){ this.notas=n; }
        public void setTotalDia(double t){ this.totalDia=t; }
    }

    public static class ResumenRep {
        private String repartidor;
        private int notas;
        private double total;
        public String getRepartidor(){ return repartidor; }
        public int getNotas(){ return notas; }
        public double getTotal(){ return total; }
        public void setRepartidor(String s){ this.repartidor=s; }
        public void setNotas(int n){ this.notas=n; }
        public void setTotal(double t){ this.total=t; }
    }

    public List<NotaVenta> listarPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        List<NotaVenta> list = new ArrayList<>();
        String sql =
            "SELECT nv.id_nota, nv.folio, nv.id_repartidor, nv.id_tienda, nv.fecha_nota, " +
            "       COALESCE(SUM((dv.cantidad_vendida - IFNULL(dv.merma,0))*dv.precio_unitario),0) AS total " +
            "FROM notas_venta nv " +
            "LEFT JOIN detalle_nota_venta dv ON dv.id_nota = nv.id_nota " +
            "WHERE DATE(nv.fecha_nota) BETWEEN ? AND ? " +
            "GROUP BY nv.id_nota, nv.folio, nv.id_repartidor, nv.id_tienda, nv.fecha_nota " +
            "ORDER BY nv.fecha_nota DESC, nv.folio DESC";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()){
                    NotaVenta n = new NotaVenta();
                    n.setIdNotaVenta(rs.getInt("id_nota"));
                    n.setFolio(rs.getInt("folio"));
                    n.setIdRepartidor(rs.getInt("id_repartidor"));
                    n.setIdTienda(rs.getInt("id_tienda"));
                    n.setFechaNota(rs.getTimestamp("fecha_nota").toLocalDateTime());
                    n.setTotal(rs.getDouble("total"));
                    list.add(n);
                }
            }
        }
        return list;
    }

    public List<ResumenDia> resumenDiario(LocalDate desde, LocalDate hasta) throws SQLException {
        List<ResumenDia> list = new ArrayList<>();
        String sql =
            "SELECT DATE(nv.fecha_nota) AS f, COUNT(DISTINCT nv.id_nota) AS notas, " +
            "       COALESCE(SUM((dv.cantidad_vendida - IFNULL(dv.merma,0))*dv.precio_unitario),0) AS total " +
            "FROM notas_venta nv " +
            "LEFT JOIN detalle_nota_venta dv ON dv.id_nota = nv.id_nota " +
            "WHERE DATE(nv.fecha_nota) BETWEEN ? AND ? " +
            "GROUP BY DATE(nv.fecha_nota) " +
            "ORDER BY f ASC";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()){
                    ResumenDia d = new ResumenDia();
                    d.setFecha(rs.getDate("f").toString());
                    d.setNotas(rs.getInt("notas"));
                    d.setTotalDia(rs.getDouble("total"));
                    list.add(d);
                }
            }
        }
        return list;
    }

    public List<ResumenRep> resumenPorRepartidor(LocalDate desde, LocalDate hasta) throws SQLException {
        List<ResumenRep> list = new ArrayList<>();
        String sql =
            "SELECT CONCAT(r.nombre_repartidor,' ',r.apellido_repartidor) AS rep, " +
            "       COUNT(DISTINCT nv.id_nota) AS notas, " +
            "       COALESCE(SUM((dv.cantidad_vendida - IFNULL(dv.merma,0))*dv.precio_unitario),0) AS total " +
            "FROM notas_venta nv " +
            "JOIN repartidores r ON r.id_repartidor = nv.id_repartidor " +
            "LEFT JOIN detalle_nota_venta dv ON dv.id_nota = nv.id_nota " +
            "WHERE DATE(nv.fecha_nota) BETWEEN ? AND ? " +
            "GROUP BY r.id_repartidor, rep " +
            "ORDER BY total DESC";
        try (Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()){
                    ResumenRep r = new ResumenRep();
                    r.setRepartidor(rs.getString("rep"));
                    r.setNotas(rs.getInt("notas"));
                    r.setTotal(rs.getDouble("total"));
                    list.add(r);
                }
            }
        }
        return list;
    }
}
