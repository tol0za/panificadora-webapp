package dao;

import modelo.Usuario;
import conexion.Conexion;
import java.sql.*;

public class UsuarioDAO {

    public Usuario validar(String usuario, String password) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, apellido_usuario, usuario, password, rol " +
                     "FROM usuarios WHERE usuario=? AND password=?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombreUsuario(rs.getString("nombre_usuario"));
                u.setApellidoUsuario(rs.getString("apellido_usuario"));
                u.setUsuario(rs.getString("usuario"));
                u.setPassword(rs.getString("password"));
                u.setRol(rs.getString("rol"));
                return u;
            }

            return null;
        }
    }
}