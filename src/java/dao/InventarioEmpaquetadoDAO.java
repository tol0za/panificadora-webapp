package dao;

import conexion.Conexion;
import modelo.InventarioEmpaquetado;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * DAO de inventario_empaquetado
 * – Mantiene compatibilidad hacia atrás.
 */
public class InventarioEmpaquetadoDAO {

    /* ------------------------------------------------------------ */
    /*  Atributo conexión                                           */
    /* ------------------------------------------------------------ */
    private Connection con;

    public InventarioEmpaquetadoDAO() throws SQLException {
        con = Conexion.getConnection();
    }

    /** Devuelve conexión abierta; si estaba cerrada crea otra. */
    private Connection getConn() throws SQLException {
        if (con == null || con.isClosed()) {
            con = Conexion.getConnection();
        }
        return con;
    }

    /* ============================================================ */
    /*  1. Histórico de movimientos                                 */
    /* ============================================================ */
    
 public List<InventarioEmpaquetado> listarMovimientos() throws SQLException {
        String sql = """
            SELECT i.*, c.nombre_empaque
              FROM inventario_empaquetado i
              JOIN catalogo_empaque c ON i.id_empaque = c.id_empaque
             ORDER BY i.fecha DESC
        """;
        List<InventarioEmpaquetado> lista = new ArrayList<>();

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventarioEmpaquetado ie = new InventarioEmpaquetado();
                ie.setIdInventario   (rs.getInt("id_inventario"));
                ie.setIdEmpaque      (rs.getInt("id_empaque"));
                ie.setNombreEmpaque  (rs.getString("nombre_empaque"));
                ie.setCantidad       (rs.getInt("cantidad"));
                ie.setMotivo         (rs.getString("motivo"));
                ie.setCantidadActual (rs.getInt("cantidad_actual"));

                Timestamp ts = rs.getTimestamp("fecha");
                if (ts != null) {
                    var ldt = ts.toLocalDateTime();
                    ie.setFecha(ldt);
                    ie.setFechaDate(java.util.Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                }
                lista.add(ie);
            }
        }
        return lista;
    }
    /* ============================================================ */
    /*  2. Métodos básicos heredados                                */
    /* ============================================================ */
    public void registrarMovimiento(InventarioEmpaquetado mov) throws SQLException {
        String sql = """
            INSERT INTO inventario_empaquetado
                   (id_empaque, cantidad, fecha, motivo, cantidad_actual)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection c = getConn()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt      (1, mov.getIdEmpaque());
                ps.setInt      (2, mov.getCantidad());
                ps.setTimestamp(3, Timestamp.valueOf(mov.getFecha()));
                ps.setString   (4, mov.getMotivo());
                ps.setInt      (5, mov.getCantidadActual());
                ps.executeUpdate();
            }
            c.commit();
        }
    }

    public int obtenerCantidadActual(int idEmp) throws SQLException {
        String sql = """
            SELECT cantidad_actual
              FROM inventario_empaquetado
             WHERE id_empaque = ?
          ORDER BY fecha DESC
             LIMIT 1
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEmp);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public BigDecimal obtenerPrecioUnitario(int idEmp) throws SQLException {
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT precio_unitario FROM catalogo_empaque WHERE id_empaque = ?")) {
            ps.setInt(1, idEmp);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public void ajustarInventario(int idEmp, int delta, String motivo) throws SQLException {
        int nuevo = obtenerCantidadActual(idEmp) + delta;
        String sql = """
            INSERT INTO inventario_empaquetado
                  (id_empaque, cantidad, fecha, motivo, cantidad_actual)
            VALUES (?, ?, NOW(), ?, ?)
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt   (1, idEmp);
            ps.setInt   (2, delta);
            ps.setString(3, motivo);
            ps.setInt   (4, nuevo);
            ps.executeUpdate();
        }
    }

    public int obtenerStockActual(int idEmp) throws SQLException {   // alias
        return obtenerCantidadActual(idEmp);
    }

    public int obtenerCantidadActualPorRepartidorYEmpaque(int idRep, int idEmp) throws SQLException {
        String sql = """
            SELECT cantidad_actual
              FROM inventario_empaquetado
             WHERE id_repartidor = ? AND id_empaque = ?
          ORDER BY fecha DESC
             LIMIT 1
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idRep);
            ps.setInt(2, idEmp);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<InventarioEmpaquetado> obtenerPorDistribucion(int idDist) throws SQLException {
        String sql = "SELECT * FROM inventario_empaquetado WHERE id_distribucion = ? ORDER BY id_empaque";
        List<InventarioEmpaquetado> lista = new ArrayList<>();

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idDist);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventarioEmpaquetado inv = new InventarioEmpaquetado();
                    inv.setIdInventario   (rs.getInt("id_inventario"));
                    inv.setIdDistribucion (rs.getInt("id_distribucion"));
                    inv.setIdEmpaque      (rs.getInt("id_empaque"));
                    inv.setCantidadActual (rs.getInt("cantidad_actual"));
                    lista.add(inv);
                }
            }
        }
        return lista;
    }

    public void actualizarStockDespuesVenta(int idDist,
                                            int idEmp,
                                            int vendidas,
                                            int merma) throws SQLException {
        int total = vendidas + merma;
        String sql = """
            UPDATE inventario_empaquetado
               SET cantidad_actual = cantidad_actual - ?
             WHERE id_distribucion = ? AND id_empaque = ?
        """;
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, total);
            ps.setInt(2, idDist);
            ps.setInt(3, idEmp);
            ps.executeUpdate();
        }
    }

    /* ============================================================ */
    /*  3. Movimientos específicos de salida / retorno              */
    /* ============================================================ */

    public void registrarMovimientoSalida(int idEmp,
                                          int cant,            // positivo; usa -cant para reversa
                                          int idDist,
                                          int idRep,
                                          String motivo) throws SQLException {

        int nuevoStock = obtenerCantidadActual(idEmp) - cant;

        String sql = """
            INSERT INTO inventario_empaquetado
                  (id_empaque, cantidad, motivo,
                   id_distribucion, id_repartidor,
                   fecha, cantidad_actual)
            VALUES (?,?,?,?,?, NOW(), ?)
        """;

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt   (1, idEmp);
            ps.setInt   (2, cant);
            ps.setString(3, motivo);
            ps.setInt   (4, idDist);
            ps.setInt   (5, idRep);
            ps.setInt   (6, nuevoStock);
            ps.executeUpdate();
        }
    }

    /** Retorno de sobrante al stock global (motivo ENTRADA_RETORNO). */
    public void regresarStockGeneral(int idEmp,
                                     int cant,
                                     int idRep) throws SQLException {

        int nuevoStock = obtenerCantidadActual(idEmp) + cant;

        String sql = """
            INSERT INTO inventario_empaquetado
                  (id_empaque, cantidad, motivo,
                   id_repartidor, fecha, cantidad_actual)
            VALUES (?, ?, 'ENTRADA_RETORNO', ?, NOW(), ?)
        """;

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idEmp);
            ps.setInt(2, cant);
            ps.setInt(3, idRep);
            ps.setInt(4, nuevoStock);
            ps.executeUpdate();
        }
    }

    /** Sobrecarga sin repartidor (idRep = 0). */
    public void regresarStockGeneral(int idEmp, int cant) throws SQLException {
        regresarStockGeneral(idEmp, cant, 0);
    }

    /* ============================================================ */
    /*  4. Retornos pendientes por repartidor / día                 */
    /* ============================================================ */
    public Map<Integer,Integer> obtenerRetornoPorRepartidorYFecha(int idRep,
                                                                  LocalDate fecha)
            throws SQLException {

        String sql = """
            SELECT id_empaque,
                   SUM(CASE
                         WHEN motivo = 'ENTRADA_RETORNO' THEN  cantidad
                         WHEN motivo = 'REABRIR_RUTA'    THEN -cantidad
                       END) AS pendientes
              FROM inventario_empaquetado
             WHERE id_repartidor = ?
               AND fecha >= ?
               AND fecha <  ?
             GROUP BY id_empaque
             HAVING pendientes > 0
        """;

        Map<Integer,Integer> map = new HashMap<>();
        LocalDate mañana = fecha.plusDays(1);

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idRep);
            ps.setTimestamp(2, Timestamp.valueOf(fecha.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(mañana.atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("id_empaque"),
                            rs.getInt("pendientes"));
                }
            }
        }
        return map;
    }
}
