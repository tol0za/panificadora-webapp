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
                u.setId(rs.getInt("id_usuario"));                    // ← id
                u.setNombre(rs.getString("nombre_usuario"));         // ← nombre
                u.setApellido(rs.getString("apellido_usuario")); // ← asegúrate de que la columna existe y el nombre coincide
                u.setUsuario(rs.getString("usuario"));               // ← usuario
                u.setPassword(rs.getString("password"));             // ← password
                u.setRol(rs.getString("rol"));                       // ← rol

                // Puedes usar apellido si agregas el campo en Usuario
                return u;
            }

            return null;
        }
    }
}
