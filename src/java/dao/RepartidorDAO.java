package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modelo.Repartidor;
import conexion.Conexion;

public class RepartidorDAO {

    private Connection conn;

    public RepartidorDAO() throws SQLException {
        conn = Conexion.getConnection();
    }

    public List<Repartidor> listarTodos() throws SQLException {
        List<Repartidor> lista = new ArrayList<>();
        String sql = "SELECT * FROM repartidores ORDER BY id_repartidor DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
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

    public Repartidor buscarPorId(int id) throws SQLException {
        Repartidor r = null;
        String sql = "SELECT * FROM repartidores WHERE id_repartidor=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    r = new Repartidor();
                    r.setIdRepartidor(rs.getInt("id_repartidor"));
                    r.setNombreRepartidor(rs.getString("nombre_repartidor"));
                    r.setApellidoRepartidor(rs.getString("apellido_repartidor"));
                    r.setTelefono(rs.getString("telefono"));
                }
            }
        }
        return r;
    }

    public void insertar(Repartidor r) throws SQLException {
        String sql = "INSERT INTO repartidores (nombre_repartidor, apellido_repartidor, telefono) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, r.getNombreRepartidor());
            stmt.setString(2, r.getApellidoRepartidor());
            stmt.setString(3, r.getTelefono());
            stmt.executeUpdate();
        }
    }

    public void actualizar(Repartidor r) throws SQLException {
        String sql = "UPDATE repartidores SET nombre_repartidor=?, apellido_repartidor=?, telefono=? WHERE id_repartidor=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, r.getNombreRepartidor());
            stmt.setString(2, r.getApellidoRepartidor());
            stmt.setString(3, r.getTelefono());
            stmt.setInt(4, r.getIdRepartidor());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM repartidores WHERE id_repartidor=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
