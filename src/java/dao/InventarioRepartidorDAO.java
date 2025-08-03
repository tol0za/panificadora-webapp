package dao;

import conexion.Conexion;
import modelo.CatalogoEmpaque;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventarioRepartidorDAO {

    public List<CatalogoEmpaque> obtenerEmpaquesConStockPorRepartidor(int idRepartidor) {
        List<CatalogoEmpaque> lista = new ArrayList<>();

        String sql = "SELECT ce.id_empaque, ce.nombre_empaque, ce.precio_unitario, ir.cantidad_restante " +
                     "FROM inventario_repartidor ir " +
                     "JOIN catalogo_empaque ce ON ir.id_empaque = ce.id_empaque " +
                     "WHERE ir.id_repartidor = ? AND ir.cantidad_restante > 0";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRepartidor);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CatalogoEmpaque empaque = new CatalogoEmpaque();
                empaque.setIdEmpaque(rs.getInt("id_empaque"));
empaque.setNombreEmpaque(rs.getString("nombre_empaque"));
empaque.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));

                empaque.setStock(rs.getInt("cantidad_restante")); // Propiedad temporal para stock
                lista.add(empaque);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}
