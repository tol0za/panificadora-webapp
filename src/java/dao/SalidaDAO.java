package dao;

import conexion.Conexion;
import modelo.CatalogoEmpaque;
import modelo.Salida;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class SalidaDAO {

    /* --------------------------------------- */
    /* Conexión                                */
    /* --------------------------------------- */
    private Connection getConn() throws SQLException {
        return Conexion.getConnection();
    }

    /* ===========================================================
     * 1. Registrar salida   (ahora retorna el PK generado)
     * ========================================================= */
    public int registrarSalida(Salida s) throws SQLException {
        String sql = """
            INSERT INTO distribucion
                   (id_repartidor, id_empaque, cantidad, fecha_distribucion)
            VALUES (?,?,?,?)
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt      (1, s.getIdRepartidor());
            ps.setInt      (2, s.getIdEmpaque());
            ps.setInt      (3, s.getCantidad());
            ps.setTimestamp(4, Timestamp.valueOf(s.getFechaDistribucion()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* ===========================================================
     * 2. Listados                                             
     * ========================================================= */
    public List<Salida> listarSalidas() throws SQLException {
        List<Salida> lista = new ArrayList<>();
        String sql = """
            SELECT d.*, r.nombre_repartidor, r.apellido_repartidor, e.nombre_empaque
              FROM distribucion d
              JOIN repartidores     r ON d.id_repartidor = r.id_repartidor
              JOIN catalogo_empaque e ON d.id_empaque    = e.id_empaque
             ORDER BY d.fecha_distribucion DESC
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapRow(rs));
        }
        return lista;
    }

    public List<Salida> listarSalidasPorFecha(String fechaISO) throws SQLException {
        List<Salida> lista = new ArrayList<>();
        String sql = """
            SELECT d.*, r.nombre_repartidor, r.apellido_repartidor, e.nombre_empaque
              FROM distribucion d
              JOIN repartidores     r ON d.id_repartidor = r.id_repartidor
              JOIN catalogo_empaque e ON d.id_empaque    = e.id_empaque
             WHERE DATE(d.fecha_distribucion) = ?
             ORDER BY r.nombre_repartidor, d.id_empaque
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fechaISO);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public List<Salida> listarSalidasPorRepartidorYFecha(int idRep, String fechaISO) throws SQLException {
        List<Salida> lista = new ArrayList<>();
        String sql = """
            SELECT d.*, r.nombre_repartidor, r.apellido_repartidor, e.nombre_empaque
              FROM distribucion d
              JOIN repartidores     r ON d.id_repartidor = r.id_repartidor
              JOIN catalogo_empaque e ON d.id_empaque    = e.id_empaque
             WHERE d.id_repartidor = ? AND DATE(d.fecha_distribucion) = ?
             ORDER BY d.id_empaque
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setString(2, fechaISO);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    /* ===========================================================
     * 3. CRUD individual                                       
     * ========================================================= */
    public Salida buscarPorId(int idDistribucion) throws SQLException {
        String sql = """
            SELECT d.*, r.nombre_repartidor, r.apellido_repartidor, e.nombre_empaque
              FROM distribucion d
              JOIN repartidores     r ON d.id_repartidor = r.id_repartidor
              JOIN catalogo_empaque e ON d.id_empaque    = e.id_empaque
             WHERE d.id_distribucion = ?
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idDistribucion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public void actualizarSalida(Salida s) throws SQLException {
        String sql = "UPDATE distribucion SET cantidad = ? WHERE id_distribucion = ?";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getCantidad());
            ps.setInt(2, s.getIdDistribucion());
            ps.executeUpdate();
        }
    }

    public void eliminarSalida(int idDistribucion) throws SQLException {
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM distribucion WHERE id_distribucion = ?")) {
            ps.setInt(1, idDistribucion);
            ps.executeUpdate();
        }
    }

    public void eliminarSalidaPorRepartidorYFecha(int idRep, String fecha) throws SQLException {
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM distribucion WHERE id_repartidor = ? AND DATE(fecha_distribucion) = ?")) {
            ps.setInt(1, idRep);
            ps.setString(2, fecha);
            ps.executeUpdate();
        }
    }

    /* ===========================================================
     * 4. Utilidades varias                                    
     * ========================================================= */
    public LocalDate obtenerFechaUltimaSalida(int idRep) throws SQLException {
        String sql = "SELECT MAX(fecha_distribucion) AS fecha FROM distribucion WHERE id_repartidor = ?";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getDate("fecha") != null) {
                    return rs.getDate("fecha").toLocalDate();
                }
            }
        }
        return null;
    }

    /* Empaques/stock para formularios AJAX -------------------- */
    
public List<CatalogoEmpaque> obtenerEmpaquesConStockPorRepartidorYFecha(
        int idRepartidor, LocalDate fecha) throws SQLException {

    List<CatalogoEmpaque> lista = new ArrayList<>();
    String sql = """
        SELECT d.id_empaque, c.nombre_empaque,
               SUM(d.cantidad) AS stock, c.precio_unitario
          FROM distribucion d
          JOIN catalogo_empaque c ON d.id_empaque = c.id_empaque
         WHERE d.id_repartidor = ?
           AND DATE(d.fecha_distribucion) = ?
         GROUP BY d.id_empaque
    """;
    try (Connection c = getConn();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1, idRepartidor);
        /* ⚠️ usa plenamente cualificado java.sql.Date */
        ps.setDate(2, java.sql.Date.valueOf(fecha));

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CatalogoEmpaque e = new CatalogoEmpaque();
                e.setIdEmpaque     (rs.getInt("id_empaque"));
                e.setNombreEmpaque (rs.getString("nombre_empaque"));
                e.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                e.setStock         (rs.getInt("stock"));
                lista.add(e);
            }
        }
    }
    return lista;
}
    /* ===========================================================
     * 5. Mappers                                               
     * ========================================================= */
    private Salida mapRow(ResultSet rs) throws SQLException {
        Salida s = new Salida();
        s.setIdDistribucion   (rs.getInt("id_distribucion"));
        s.setIdRepartidor     (rs.getInt("id_repartidor"));
        s.setIdEmpaque        (rs.getInt("id_empaque"));
        s.setCantidad         (rs.getInt("cantidad"));

        Timestamp ts = rs.getTimestamp("fecha_distribucion");
        s.setFechaDistribucion     (ts.toLocalDateTime());
        s.setFechaDistribucionDate (ts);

        // alias de joins
        if (hasColumn(rs, "nombre_repartidor"))
            s.setNombreRepartidor (rs.getString("nombre_repartidor"));
        if (hasColumn(rs, "apellido_repartidor"))
            s.setApellidoRepartidor(rs.getString("apellido_repartidor"));
        if (hasColumn(rs, "nombre_empaque"))
            s.setNombreEmpaque    (rs.getString("nombre_empaque"));
        return s;
    }

    private boolean hasColumn(ResultSet rs, String col) {
        try {
            rs.findColumn(col);
            return true;
        } catch (SQLException ignored) { return false; }
    }
}
