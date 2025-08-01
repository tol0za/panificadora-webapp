package dao;

import modelo.Salida;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import conexion.Conexion;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class SalidaDAO {
    private Connection conn;

    public SalidaDAO() throws SQLException {
        conn = Conexion.getConnection();
    }

    public void registrarSalida(Salida s) throws SQLException {
        String sql = "INSERT INTO distribucion (id_repartidor, id_empaque, cantidad, fecha_distribucion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getIdRepartidor());
            ps.setInt(2, s.getIdEmpaque());
            ps.setInt(3, s.getCantidad());
            ps.setTimestamp(4, Timestamp.valueOf(s.getFechaDistribucion()));
            ps.executeUpdate();
        }
    }

    public List<Salida> listarSalidas() throws SQLException {
        List<Salida> lista = new ArrayList<>();
        String sql = "SELECT d.*, r.nombre_repartidor, r.apellido_repartidor, e.nombre_empaque " +
                "FROM distribucion d " +
                "JOIN repartidores r ON d.id_repartidor = r.id_repartidor " +
                "JOIN catalogo_empaque e ON d.id_empaque = e.id_empaque " +
                "ORDER BY d.fecha_distribucion DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Salida s = new Salida();
                s.setIdDistribucion(rs.getInt("id_distribucion"));
                s.setIdRepartidor(rs.getInt("id_repartidor"));
                s.setIdEmpaque(rs.getInt("id_empaque"));
                s.setCantidad(rs.getInt("cantidad"));

                Timestamp ts = rs.getTimestamp("fecha_distribucion");
                s.setFechaDistribucion(ts.toLocalDateTime());
                s.setFechaDistribucionDate(ts); // Para JSTL

                s.setNombreRepartidor(rs.getString("nombre_repartidor"));
                s.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                s.setNombreEmpaque(rs.getString("nombre_empaque"));

                lista.add(s);
            }
        }
        return lista;
    }

    public List<Salida> listarSalidasPorFecha(String fechaISO) throws SQLException {
        List<Salida> lista = new ArrayList<>();
        String sql = "SELECT d.*, r.nombre_repartidor, r.apellido_repartidor, e.nombre_empaque " +
                "FROM distribucion d " +
                "JOIN repartidores r ON d.id_repartidor = r.id_repartidor " +
                "JOIN catalogo_empaque e ON d.id_empaque = e.id_empaque " +
                "WHERE DATE(d.fecha_distribucion) = ? " +
                "ORDER BY r.nombre_repartidor, d.id_empaque";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fechaISO);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Salida s = new Salida();
                s.setIdDistribucion(rs.getInt("id_distribucion"));
                s.setIdRepartidor(rs.getInt("id_repartidor"));
                s.setIdEmpaque(rs.getInt("id_empaque"));
                s.setCantidad(rs.getInt("cantidad"));
                Timestamp ts = rs.getTimestamp("fecha_distribucion");
                s.setFechaDistribucion(ts.toLocalDateTime());
                s.setFechaDistribucionDate(ts);
                s.setNombreRepartidor(rs.getString("nombre_repartidor"));
                s.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                s.setNombreEmpaque(rs.getString("nombre_empaque"));
                lista.add(s);
            }
        }
        return lista;
    }

    public List<Salida> listarSalidasPorRepartidorYFecha(int idRepartidor, String fechaISO) throws SQLException {
        List<Salida> lista = new ArrayList<>();
        String sql = "SELECT d.*, r.nombre_repartidor, r.apellido_repartidor, e.nombre_empaque " +
                "FROM distribucion d " +
                "JOIN repartidores r ON d.id_repartidor = r.id_repartidor " +
                "JOIN catalogo_empaque e ON d.id_empaque = e.id_empaque " +
                "WHERE d.id_repartidor = ? AND DATE(d.fecha_distribucion) = ? " +
                "ORDER BY d.id_empaque";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setString(2, fechaISO);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Salida s = new Salida();
                s.setIdDistribucion(rs.getInt("id_distribucion"));
                s.setIdRepartidor(rs.getInt("id_repartidor"));
                s.setIdEmpaque(rs.getInt("id_empaque"));
                s.setCantidad(rs.getInt("cantidad"));
                Timestamp ts = rs.getTimestamp("fecha_distribucion");
                s.setFechaDistribucion(ts.toLocalDateTime());
                s.setFechaDistribucionDate(ts);
                s.setNombreRepartidor(rs.getString("nombre_repartidor"));
                s.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                s.setNombreEmpaque(rs.getString("nombre_empaque"));
                lista.add(s);
            }
        }
        return lista;
    }

    // ELIMINAR UN ARTÍCULO INDIVIDUAL (detalle múltiple)
    public void eliminarSalida(int idDistribucion) throws SQLException {
        String sql = "DELETE FROM distribucion WHERE id_distribucion = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDistribucion);
            ps.executeUpdate();
        }
    }

    // ELIMINAR TODOS LOS ARTÍCULOS DE UN REPARTIDOR EN UNA FECHA
    public void eliminarSalidaPorRepartidorYFecha(int idRepartidor, String fecha) throws SQLException {
        String sql = "DELETE FROM distribucion WHERE id_repartidor = ? AND DATE(fecha_distribucion) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setString(2, fecha);
            ps.executeUpdate();
        }
    }

    public Salida buscarPorId(int idDistribucion) throws SQLException {
        String sql = "SELECT d.*, r.nombre_repartidor, r.apellido_repartidor, e.nombre_empaque " +
                "FROM distribucion d " +
                "JOIN repartidores r ON d.id_repartidor = r.id_repartidor " +
                "JOIN catalogo_empaque e ON d.id_empaque = e.id_empaque " +
                "WHERE d.id_distribucion = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDistribucion);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Salida s = new Salida();
                s.setIdDistribucion(rs.getInt("id_distribucion"));
                s.setIdRepartidor(rs.getInt("id_repartidor"));
                s.setIdEmpaque(rs.getInt("id_empaque"));
                s.setCantidad(rs.getInt("cantidad"));
                Timestamp ts = rs.getTimestamp("fecha_distribucion");
                s.setFechaDistribucion(ts.toLocalDateTime());
                s.setFechaDistribucionDate(ts);
                s.setNombreRepartidor(rs.getString("nombre_repartidor"));
                s.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                s.setNombreEmpaque(rs.getString("nombre_empaque"));
                return s;
            }
        }
        return null;
    }

    public void actualizarSalida(Salida s) throws SQLException {
        String sql = "UPDATE distribucion SET cantidad=? WHERE id_distribucion=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getCantidad());
            ps.setInt(2, s.getIdDistribucion());
            ps.executeUpdate();
        }
    }
    // ✅ NUEVO MÉTODO: Obtener empaques con stock actual por repartidor
    public List<Map<String, Object>> obtenerEmpaquesPorRepartidor(int idRepartidor) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT d.id_empaque, e.nombre_empaque, e.precio_unitario,
                   COALESCE(i.cantidad_actual, 0) AS stock
            FROM distribucion d
            INNER JOIN catalogo_empaque e ON d.id_empaque = e.id_empaque
            LEFT JOIN (
                SELECT id_empaque, cantidad_actual
                FROM inventario_empaquetado
                WHERE id_repartidor = ?
                ORDER BY fecha DESC
            ) i ON i.id_empaque = d.id_empaque
            WHERE d.id_repartidor = ?
            GROUP BY d.id_empaque
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRepartidor);
            stmt.setInt(2, idRepartidor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("id_empaque", rs.getInt("id_empaque"));
                    fila.put("nombre_empaque", rs.getString("nombre_empaque"));
                    fila.put("precio_unitario", rs.getBigDecimal("precio_unitario"));
                    fila.put("stock", rs.getInt("stock"));
                    lista.add(fila);
                }
            }
        }
        return lista;
    }
}
