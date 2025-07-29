package dao;

import conexion.Conexion;
import modelo.CatalogoPan;
import java.sql.*;
import java.util.*;

public class CatalogoPanDAO {
    private final Connection con;

    public CatalogoPanDAO() throws SQLException {
        this.con = Conexion.getConnection();
    }

    public List<CatalogoPan> findAll() throws SQLException {
        List<CatalogoPan> lista = new ArrayList<>();
        String sql = "SELECT * FROM catalogo_pan";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CatalogoPan pan = new CatalogoPan();
                pan.setIdPan(rs.getInt("id_pan"));
                pan.setNombrePan(rs.getString("nombre_pan"));
                lista.add(pan);
            }
        }
        return lista;
    }

    public CatalogoPan findById(int id) throws SQLException {
        String sql = "SELECT * FROM catalogo_pan WHERE id_pan=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CatalogoPan pan = new CatalogoPan();
                    pan.setIdPan(rs.getInt("id_pan"));
                    pan.setNombrePan(rs.getString("nombre_pan"));
                    return pan;
                }
            }
        }
        return null;
    }

    public void insert(CatalogoPan pan) throws SQLException {
        String sql = "INSERT INTO catalogo_pan (nombre_pan) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pan.getNombrePan());
            ps.executeUpdate();
        }
    }

    public void update(CatalogoPan pan) throws SQLException {
        String sql = "UPDATE catalogo_pan SET nombre_pan=? WHERE id_pan=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pan.getNombrePan());
            ps.setInt(2, pan.getIdPan());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM catalogo_pan WHERE id_pan=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
