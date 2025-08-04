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

public void descontar(int idRep, int idEmp, int piezas) throws SQLException {

    final String sql = """
        UPDATE inventario_repartidor
           SET cantidad_vendida  = IFNULL(cantidad_vendida,0) + ?,
               cantidad_restante = cantidad_restante - ?
         WHERE id_repartidor = ?
           AND id_empaque    = ?
           AND cantidad_restante >= ?          -- stock suficiente
    """;

    try (Connection c = getConn();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1, piezas);
        ps.setInt(2, piezas);
        ps.setInt(3, idRep);
        ps.setInt(4, idEmp);
        ps.setInt(5, piezas);

        /* Si no se actualizó ninguna fila ⇒ no había inventario */
        if (ps.executeUpdate() == 0) {
            throw new SQLException(
                "Sin inventario suficiente para rep=" + idRep + ", emp=" + idEmp);
        }
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
public int getRestante(int idRep,int idEmp) throws SQLException {
    String sql = "SELECT cantidad_restante FROM inventario_repartidor " +
                 "WHERE id_repartidor=? AND id_empaque=?";
    try(Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)){
        ps.setInt(1,idRep); ps.setInt(2,idEmp);
        try(ResultSet rs=ps.executeQuery()){ return rs.next()?rs.getInt(1):0; }
    }
}
/** Deja restante = restante - piezas (normalmente para retornar sobrante) */
public void bajarRestante(int idRep,int idEmp,int piezas) throws SQLException{
    String sql = """
        UPDATE inventario_repartidor
           SET cantidad_restante = cantidad_restante - ?
         WHERE id_repartidor = ? AND id_empaque = ?
    """;
    try(Connection c=getConn(); PreparedStatement ps=c.prepareStatement(sql)){
        ps.setInt(1,piezas);
        ps.setInt(2,idRep);
        ps.setInt(3,idEmp);
        ps.executeUpdate();
    }
}
/**
 * Asegura que el repartidor tenga al menos <piezasNecesarias> unidades
 * de <idEmpaque>. Si no existe registro lo crea; si existe y no alcanza,
 * suma la diferencia.  Devuelve la cantidad agregada (0 si ya alcanzaba).
 */
public int garantizarStock(int idRep,int idEmp,int piezasNecesarias) throws SQLException {

    int restante = getRestante(idRep, idEmp);

    if (restante >= piezasNecesarias) return 0;

    int faltan = piezasNecesarias - restante;            // p.ej. 250
    insertarInicial(idRep, idEmp, faltan);               // crea / acumula

    return faltan;                                       // <- NUEVO
}
/* +--- en InventarioRepartidorDAO ---+ */
public void agregarRestante(int idRep,
                            int idEmp,
                            int piezas) throws SQLException {

    String sql = """
        UPDATE inventario_repartidor
           SET cantidad_restante = cantidad_restante + ?
         WHERE id_repartidor = ? AND id_empaque = ?
    """;
    try (Connection c = getConn();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1, piezas);
        ps.setInt(2, idRep);
        ps.setInt(3, idEmp);

        /* si aún no existe registro: lo crea en 0 vendido / 0 distribuido */
        if (ps.executeUpdate() == 0) {
            insertarInicial(idRep, idEmp, piezas);   // crea fila nueva
            /* insertarInicial deja distribuida = piezas,
               pero solo en este caso (fila nueva) es correcto.            */
        }
    }
}
public void consumirRestante(int idRep,
                             int idEmp,
                             int piezas) throws SQLException {
    String sql = """
        UPDATE inventario_repartidor
           SET cantidad_vendida  = IFNULL(cantidad_vendida,0) + ?,
               cantidad_restante = GREATEST(cantidad_restante - ?, 0)
         WHERE id_repartidor = ? AND id_empaque = ?
    """;
    try (Connection c = getConn();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, piezas);
        ps.setInt(2, piezas);
        ps.setInt(3, idRep);
        ps.setInt(4, idEmp);
        ps.executeUpdate();
    }
}
}
