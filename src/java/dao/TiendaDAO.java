package dao;

import conexion.Conexion;
import modelo.Tienda;
import java.sql.*;
import java.util.*;

public class TiendaDAO {
    private Connection conn;
    public TiendaDAO() throws SQLException {
        this.conn = Conexion.getConnection();
    }

    public List<Tienda> listar() throws SQLException {
        List<Tienda> lista = new ArrayList<>();
        String sql = "SELECT * FROM tiendas ORDER BY id_tienda DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Tienda t = new Tienda(
                    rs.getInt("id_tienda"),
                    rs.getString("nombre_tienda"),
                    rs.getString("direccion"),
                    rs.getString("telefono")
                );
                lista.add(t);
            }
        }
        return lista;
    }
public List<Tienda> listarTodas() throws SQLException {
    return listar();           // o  return obtenerTodas();
}
    public Tienda buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM tiendas WHERE id_tienda=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Tienda(
                        rs.getInt("id_tienda"),
                        rs.getString("nombre_tienda"),
                        rs.getString("direccion"),
                        rs.getString("telefono")
                    );
                }
            }
        }
        return null;
    }

    public void insertar(Tienda t) throws SQLException {
        String sql = "INSERT INTO tiendas (nombre_tienda, direccion, telefono) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getNombre());
            stmt.setString(2, t.getDireccion());
            stmt.setString(3, t.getTelefono());
            stmt.executeUpdate();
        }
    }

    public void actualizar(Tienda t) throws SQLException {
        String sql = "UPDATE tiendas SET nombre_tienda=?, direccion=?, telefono=? WHERE id_tienda=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getNombre());
            stmt.setString(2, t.getDireccion());
            stmt.setString(3, t.getTelefono());
            stmt.setInt(4, t.getIdTienda());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM tiendas WHERE id_tienda=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // === Para compatibilidad con findAll() ===
    public List<Tienda> findAll() throws SQLException {
        return listar();
    }
    
   public List<Tienda> obtenerTodas() throws SQLException {
    List<Tienda> lista = new ArrayList<>();
    String sql = "SELECT * FROM tiendas";
    try (Connection conn = Conexion.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            Tienda t = new Tienda();
            t.setIdTienda(rs.getInt("id_tienda"));
            t.setNombreTienda(rs.getString("nombre_tienda"));
            t.setDireccion(rs.getString("direccion"));
            t.setTelefono(rs.getString("telefono"));
            t.setZona(rs.getString("zona"));
            lista.add(t);
        }
    }
    return lista;
}
 
    
}
