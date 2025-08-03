package dao;

import conexion.Conexion;
import modelo.CatalogoEmpaque;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla <code>inventario_repartidor</code>.
 * Combina los métodos que ya utilizabas con los nuevos requeridos por
 * el módulo de Notas de Venta. NO contiene lógica de inventario global
 * (eso está en <code>InventarioEmpaquetadoDAO</code>).
 */
public class InventarioRepartidorDAO {

    /* ===================================================== */
    /*  Conexión                                             */
    /* ===================================================== */
    private Connection getConn() throws SQLException {
        return Conexion.getConnection();
    }

    /* ===================================================== */
    /*  Método pre‑existente en tu proyecto                  */
    /* ===================================================== */
    /**
     * Devuelve los empaques con stock > 0 para un repartidor
     * (usado en varios módulos del sistema).
     */
    public List<CatalogoEmpaque> obtenerEmpaquesConStockPorRepartidor(int idRepartidor) {
        List<CatalogoEmpaque> lista = new ArrayList<>();
        String sql = "SELECT ce.id_empaque, ce.nombre_empaque, ce.precio_unitario, ir.cantidad_restante " +
                     "FROM inventario_repartidor ir " +
                     "JOIN catalogo_empaque ce ON ir.id_empaque = ce.id_empaque " +
                     "WHERE ir.id_repartidor = ? AND ir.cantidad_restante > 0";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CatalogoEmpaque empaque = new CatalogoEmpaque();
                    empaque.setIdEmpaque(rs.getInt("id_empaque"));
                    empaque.setNombreEmpaque(rs.getString("nombre_empaque"));
                    empaque.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    empaque.setStock(rs.getInt("cantidad_restante")); // campo auxiliar
                    lista.add(empaque);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /* ===================================================== */
    /*  MÉTODOS NUEVOS – módulo Notas de Venta               */
    /* ===================================================== */

    /**
     * Descuenta <code>cantidad</code> piezas (vendidas + merma) del inventario
     * del repartidor para un empaque.
     */
    public void descontar(int idRepartidor, int idEmpaque, int cantidad) throws SQLException {
        String sql = "UPDATE inventario_repartidor " +
                     "SET cantidad_vendida = IFNULL(cantidad_vendida,0) + ?, " +
                     "    cantidad_restante = cantidad_restante - ? " +
                     "WHERE id_repartidor = ? AND id_empaque = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, cantidad);
            ps.setInt(3, idRepartidor);
            ps.setInt(4, idEmpaque);
            ps.executeUpdate();
        }
    }

    /**
     * Devuelve piezas al inventario (al editar/eliminar nota o al cerrar ruta).
     */
    public void devolver(int idRepartidor, int idEmpaque, int cantidad) throws SQLException {
        String sql = "UPDATE inventario_repartidor " +
                     "SET cantidad_vendida = IFNULL(cantidad_vendida,0) - ?, " +
                     "    cantidad_restante = cantidad_restante + ? " +
                     "WHERE id_repartidor = ? AND id_empaque = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, cantidad);
            ps.setInt(3, idRepartidor);
            ps.setInt(4, idEmpaque);
            ps.executeUpdate();
        }
    }

    /**
     * Inserta inventario inicial o acumula si ya existe (usado al generar salida).
     */
    public void insertarInicial(int idRepartidor, int idEmpaque, int cantidad) throws SQLException {
        String sql = "INSERT INTO inventario_repartidor " +
                     "(id_repartidor, id_empaque, cantidad_distribuida, cantidad_vendida, cantidad_mermada, cantidad_restante) " +
                     "VALUES (?,?,?,0,0,?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "cantidad_distribuida = cantidad_distribuida + VALUES(cantidad_distribuida), " +
                     "cantidad_restante    = cantidad_restante    + VALUES(cantidad_restante)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setInt(2, idEmpaque);
            ps.setInt(3, cantidad);
            ps.setInt(4, cantidad);
            ps.executeUpdate();
        }
    }

    /**
     * Devuelve el stock restante de un empaque para un repartidor.
     */
    public int getRestante(int idRepartidor, int idEmpaque) throws SQLException {
        String sql = "SELECT cantidad_restante FROM inventario_repartidor WHERE id_repartidor=? AND id_empaque=?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setInt(2, idEmpaque);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
