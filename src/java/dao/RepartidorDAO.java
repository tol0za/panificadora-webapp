package dao;

import conexion.Conexion;
import modelo.Repartidor;
import java.sql.*;
import java.util.*;

public class RepartidorDAO {
    private Connection conn;

    public RepartidorDAO() throws SQLException {
        conn = Conexion.getConnection();
    }

    public void insertar(Repartidor r) throws SQLException {
        String sql = "INSERT INTO repartidores (nombre_repartidor, apellido_repartidor, telefono) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getNombreRepartidor());
            ps.setString(2, r.getApellidoRepartidor());
            ps.setString(3, r.getTelefono());
            ps.executeUpdate();
        }
    }

    public void actualizar(Repartidor r) throws SQLException {
        String sql = "UPDATE repartidores SET nombre_repartidor=?, apellido_repartidor=?, telefono=? WHERE id_repartidor=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getNombreRepartidor());
            ps.setString(2, r.getApellidoRepartidor());
            ps.setString(3, r.getTelefono());
            ps.setInt(4, r.getIdRepartidor());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM repartidores WHERE id_repartidor=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Repartidor buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM repartidores WHERE id_repartidor=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Repartidor r = new Repartidor();
                    r.setIdRepartidor(rs.getInt("id_repartidor"));
                    r.setNombreRepartidor(rs.getString("nombre_repartidor"));
                    r.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                    r.setTelefono(rs.getString("telefono"));
                    return r;
                }
            }
        }
        return null;
    }

    public List<Repartidor> listar() throws SQLException {
        List<Repartidor> lista = new ArrayList<>();
        String sql = "SELECT * FROM repartidores ORDER BY id_repartidor DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Repartidor r = new Repartidor();
                r.setIdRepartidor(rs.getInt("id_repartidor"));
                r.setNombreRepartidor(rs.getString("nombre_repartidor"));
                r.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                r.setTelefono(rs.getString("telefono"));
                lista.add(r);
            }
        }
        return lista;
    }

    // Listar solo repartidores que registraron salida hoy
    public List<Repartidor> obtenerRepartidoresConSalidaHoy() throws SQLException {
        List<Repartidor> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT r.* FROM repartidores r " +
                     "JOIN distribucion d ON r.id_repartidor = d.id_repartidor " +
                     "WHERE DATE(d.fecha_distribucion) = CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Repartidor r = new Repartidor();
                r.setIdRepartidor(rs.getInt("id_repartidor"));
                r.setNombreRepartidor(rs.getString("nombre_repartidor"));
                r.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                r.setTelefono(rs.getString("telefono"));
                lista.add(r);
            }
        }
        return lista;
    }
    
    public List<Repartidor> findAll() throws SQLException {
    List<Repartidor> lista = new ArrayList<>();
    String sql = "SELECT * FROM repartidores";
    try (Connection conn = Conexion.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            Repartidor r = new Repartidor();
            r.setIdRepartidor(rs.getInt("id_repartidor"));
            r.setNombreRepartidor(rs.getString("nombre_repartidor"));
            r.setApellidoRepartidor(rs.getString("apellido_repartidor"));
            r.setTelefono(rs.getString("telefono"));
            lista.add(r);
        }
    }
    return lista;
}

}
