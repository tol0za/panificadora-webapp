package dao;

import conexion.Conexion;
import modelo.Salida;
import java.sql.*;
import java.util.*;

public class SalidaDAO {
    private Connection con;

    public SalidaDAO() throws SQLException {
        con = Conexion.getConnection();  // Conexión establecida correctamente
    }

    // Inserta una salida y actualiza inventarios con transacción para mantener consistencia
    public void insertarSalida(Salida salida) throws SQLException {
        String sqlDistribucion = "INSERT INTO distribucion (id_repartidor, id_empaque, cantidad) VALUES (?, ?, ?)";
        String sqlInventarioEmpaquetado = "UPDATE inventario_empaquetado SET cantidad_actual = cantidad_actual - ? WHERE id_empaque = ?";
        String sqlInventarioRepartidor = "INSERT INTO inventario_repartidor (id_repartidor, id_empaque, cantidad_distribuida, cantidad_restante) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE cantidad_distribuida = cantidad_distribuida + VALUES(cantidad_distribuida), cantidad_restante = cantidad_restante + VALUES(cantidad_restante)";

        con.setAutoCommit(false); // Inicia transacción

        try (
            PreparedStatement psDistribucion = con.prepareStatement(sqlDistribucion);
            PreparedStatement psInventarioEmpaquetado = con.prepareStatement(sqlInventarioEmpaquetado);
            PreparedStatement psInventarioRepartidor = con.prepareStatement(sqlInventarioRepartidor);
        ) {
            // Inserta movimiento en distribucion
            psDistribucion.setInt(1, salida.getIdRepartidor());
            psDistribucion.setInt(2, salida.getIdEmpaque());
            psDistribucion.setInt(3, salida.getCantidad());
            psDistribucion.executeUpdate();

            // Actualiza inventario general (empaquetado)
            psInventarioEmpaquetado.setInt(1, salida.getCantidad());
            psInventarioEmpaquetado.setInt(2, salida.getIdEmpaque());
            psInventarioEmpaquetado.executeUpdate();

            // Actualiza inventario específico del repartidor
            psInventarioRepartidor.setInt(1, salida.getIdRepartidor());
            psInventarioRepartidor.setInt(2, salida.getIdEmpaque());
            psInventarioRepartidor.setInt(3, salida.getCantidad());
            psInventarioRepartidor.setInt(4, salida.getCantidad());
            psInventarioRepartidor.executeUpdate();

            con.commit(); // Confirma transacción

        } catch (SQLException e) {
            con.rollback(); // Deshace todo si falla alguna instrucción
            throw e;
        } finally {
            con.setAutoCommit(true); // Restaura modo automático
        }
    }

    // Devuelve la lista de salidas ordenada por fecha descendente
    public List<Salida> listarSalidas() throws SQLException {
        List<Salida> salidas = new ArrayList<>();
        String sql = "SELECT * FROM distribucion ORDER BY fecha_distribucion DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Salida s = new Salida();
                s.setIdDistribucion(rs.getInt("id_distribucion"));
                s.setIdRepartidor(rs.getInt("id_repartidor"));
                s.setIdEmpaque(rs.getInt("id_empaque"));
                s.setCantidad(rs.getInt("cantidad"));
                s.setFechaDistribucion(rs.getTimestamp("fecha_distribucion").toLocalDateTime());
                salidas.add(s);
            }
        }
        return salidas;
    }
}