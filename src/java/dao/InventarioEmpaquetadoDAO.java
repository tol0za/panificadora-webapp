package dao;

import conexion.Conexion;
import modelo.InventarioEmpaquetado;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * DAO de la tabla <code>inventario_empaquetado</code>.
 * - Mantiene compatibilidad con todos los métodos que ya usabas.
 * - Las nuevas funciones devuelven el stock SIN necesitar tabla auxiliar:
 *   el saldo se calcula a partir del último movimiento insertado.
 */
public class InventarioEmpaquetadoDAO {

    /* ------------------------------------------------------------ */
    /*  Conexión                                                    */
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
    /*  1. Listado histórico de movimientos                         */
    /* ============================================================ */
    public List<InventarioEmpaquetado> listarMovimientos() throws SQLException {

        String sql = """
            SELECT i.*, c.nombre_empaque
              FROM inventario_empaquetado i
              JOIN catalogo_empaque c USING(id_empaque)
             ORDER BY i.fecha DESC
        """;

        List<InventarioEmpaquetado> lista = new ArrayList<>();
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventarioEmpaquetado ie = new InventarioEmpaquetado();
                ie.setIdInventario   (rs.getInt   ("id_inventario"));
                ie.setIdEmpaque      (rs.getInt   ("id_empaque"));
                ie.setNombreEmpaque  (rs.getString("nombre_empaque"));
                ie.setCantidad       (rs.getInt   ("cantidad"));
                ie.setMotivo         (rs.getString("motivo"));
                ie.setCantidadActual (rs.getInt   ("cantidad_actual"));

                Timestamp ts = rs.getTimestamp("fecha");
                if (ts != null) {
                    var ldt = ts.toLocalDateTime();
                    ie.setFecha(ldt);
                    //ie.setFechaDate(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                }
                ie.setIdDistribucion((Integer) rs.getObject("id_distribucion"));
                ie.setIdRepartidor  ((Integer) rs.getObject("id_repartidor"));
                lista.add(ie);
            }
        }
        return lista;
    }
    // === NUEVO: devuelve piezas al stock como ajuste de ENTRADA ===
public void registrarMovimientoAjusteEntrada(int idEmpaque, int cantidad, String motivo, String descripcion) throws SQLException {
    if (cantidad <= 0) return;
    String sql = "INSERT INTO inventario_empaquetado " +
                 "(id_empaque, cantidad, tipo, motivo, descripcion, fecha_hora) " +
                 "VALUES (?, ?, 'ENTRADA', ?, ?, NOW())";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idEmpaque);
        ps.setInt(2, cantidad);
        ps.setString(3, motivo);
        ps.setString(4, descripcion);
        ps.executeUpdate();
    }
}
    
   // NUEVO: movimientos por empaque (para el modal)
public List<InventarioEmpaquetado> listarMovimientosPorEmpaque(int idEmpaque) throws SQLException {
    String sql = """
        SELECT id_inventario, id_empaque, id_distribucion, id_repartidor,
               cantidad, fecha, motivo, cantidad_actual
          FROM inventario_empaquetado
         WHERE id_empaque = ?
         ORDER BY fecha DESC
    """;
    List<InventarioEmpaquetado> lista = new ArrayList<>();
    try (Connection c = getConn();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, idEmpaque);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InventarioEmpaquetado ie = new InventarioEmpaquetado();
                ie.setIdInventario   (rs.getInt("id_inventario"));
                ie.setIdEmpaque      (rs.getInt("id_empaque"));
                ie.setIdDistribucion ((Integer) rs.getObject("id_distribucion"));
                ie.setIdRepartidor   ((Integer) rs.getObject("id_repartidor"));
                ie.setCantidad       (rs.getInt("cantidad"));
                ie.setMotivo         (rs.getString("motivo"));
                ie.setCantidadActual (rs.getInt("cantidad_actual"));
                var ts = rs.getTimestamp("fecha");
                ie.setFecha(ts != null ? ts.toLocalDateTime() : null);
                lista.add(ie);
            }
        }
    }
    return lista;
} 

    /* ============================================================ */
    /*  2. Métodos básicos heredados                                */
    /* ============================================================ */

    /** Inserta un movimiento COMPLETO.  Sigue siendo usado por el resto de métodos. */
    public void registrarMovimiento(InventarioEmpaquetado mov) throws SQLException {

        String sql = """
            INSERT INTO inventario_empaquetado
                   (id_empaque,id_distribucion,id_repartidor,
                    cantidad,fecha,motivo,cantidad_actual)
            VALUES (?,?,?,?,?,?,?)
        """;

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt      (1, mov.getIdEmpaque());
            ps.setObject   (2, mov.getIdDistribucion(), Types.INTEGER);
            ps.setObject   (3, mov.getIdRepartidor(),   Types.INTEGER);
            ps.setInt      (4, mov.getCantidad());
            ps.setTimestamp(5, Timestamp.valueOf(mov.getFecha()));
            ps.setString   (6, mov.getMotivo());
            ps.setInt      (7, mov.getCantidadActual());
            ps.executeUpdate();
        }
    }

    /** Stock en bodega (ultimo movimiento). */
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

    /** Precio unitario del catálogo (sin cambios). */
    public BigDecimal obtenerPrecioUnitario(int idEmp) throws SQLException {
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT precio_unitario FROM catalogo_empaque WHERE id_empaque=?")) {
            ps.setInt(1, idEmp);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    /** Registro libre de ajustes (se mantiene para compatibilidad). */
    public void ajustarInventario(int idEmp, int delta, String motivo) throws SQLException {
        int nuevo = obtenerCantidadActual(idEmp) + delta;
        String sql = """
            INSERT INTO inventario_empaquetado
                  (id_empaque,cantidad,fecha,motivo,cantidad_actual)
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

    /* alias usados en otras partes del sistema */
    public int obtenerStockActual(int idEmp) throws SQLException { return obtenerCantidadActual(idEmp); }

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
        String sql = "SELECT * FROM inventario_empaquetado WHERE id_distribucion=? ORDER BY id_empaque";
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
    /*  3. Movimientos de salidas / retornos específicos            */
    /* ============================================================ */

    
public void registrarMovimientoSalida(int idEmpaque,
                                      int cantidad,          // siempre POSITIVO
                                      int idDistribucion,
                                      int idRepartidor,
                                      String motivo,
                                      int nuevoStock) throws SQLException {

    String sql = """
        INSERT INTO inventario_empaquetado
              (id_empaque, cantidad, motivo,
               id_distribucion, id_repartidor,
               fecha, cantidad_actual)
        VALUES (?,?,?,?,?, NOW(), ?)
    """;

    try (Connection c = getConn();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt   (1, idEmpaque);
        ps.setInt   (2, -Math.abs(cantidad));      // SALIDA ⇒ negativa
        ps.setString(3, motivo);
        ps.setObject(4, (idDistribucion == 0 ? null : idDistribucion), Types.INTEGER);
        ps.setObject(5, (idRepartidor   == 0 ? null : idRepartidor),   Types.INTEGER);
        ps.setInt   (6, nuevoStock);               // existencia real tras la operación
        ps.executeUpdate();
    }
}

/* ------------------------------------------------------------------
 * (B) FIRMA LEGACY – 5 parámetros  (compatibilidad hacia atrás)
 *     • Calcula el stock y delega a la extendida
 *     • Deja intacto el código viejo que ya llamaba a esta firma
 * ------------------------------------------------------------------ */
public void registrarMovimientoSalida(int idEmpaque,
                                      int cantidad,
                                      int idDistribucion,
                                      int idRepartidor,
                                      String motivo) throws SQLException {

    int stockPosterior = obtenerCantidadActual(idEmpaque) - Math.abs(cantidad);
    registrarMovimientoSalida(idEmpaque, cantidad,
                              idDistribucion, idRepartidor,
                              motivo, stockPosterior);
}

public void regresarStockGeneral(int idEmp, int cant, int idRep) throws SQLException {
    int nuevoStock = obtenerCantidadActual(idEmp) + cant;
    String sql = """
        INSERT INTO inventario_empaquetado
              (id_empaque,cantidad,motivo,id_repartidor,fecha,cantidad_actual)
        VALUES (?, ?, 'ENTRADA_RETORNO', ?, NOW(), ?)
    """;
    try (Connection c = getConn();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1, idEmp);
        ps.setInt(2, cant);
        // 👇 si no tienes repartidor, manda NULL (no 0)
        ps.setObject(3, (idRep == 0 ? null : idRep), java.sql.Types.INTEGER);
        ps.setInt(4, nuevoStock);
        ps.executeUpdate();
    }
}

// mantiene compatibilidad; ahora ya no “rompe” porque el de arriba convierte 0→NULL
public void regresarStockGeneral(int idEmp, int cant) throws SQLException {
    regresarStockGeneral(idEmp, cant, 0);
}


    /* ============================================================ */
    /*  4. Retornos pendientes por repartidor / día                 */
    /* ============================================================ */

public Map<Integer,Integer> obtenerRetornoPorRepartidorYFecha(int idRep,
                                                              LocalDate fecha) throws SQLException {
    String sql = """
        SELECT id_empaque,
               SUM(cantidad) AS pendientes
          FROM inventario_empaquetado
         WHERE id_repartidor = ?
           AND motivo IN ('ENTRADA_RETORNO','REABRIR_RUTA')  -- ← sólo estos dos
           AND fecha >= ?  AND fecha < ?
         GROUP BY id_empaque
         HAVING pendientes > 0
    """;
    Map<Integer,Integer> map = new HashMap<>();
    try (Connection c = getConn();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, idRep);
        ps.setTimestamp(2, Timestamp.valueOf(fecha.atStartOfDay()));
        ps.setTimestamp(3, Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("id_empaque"), rs.getInt("pendientes"));
            }
        }
    }
    return map;
}

    /* ============================================================ */
    /*  5. NUEVOS helpers: mismos nombres que usará DistribucionDAO */
    /* ============================================================ */

    /* --- interno reutilizable ----------------------------------- */
    private int moverInventarioSimple(int idEmpaque,
                                      int delta,
                                      String motivo) throws SQLException {

        int stockAnterior = obtenerCantidadActual(idEmpaque);
        int nuevoStock    = stockAnterior + delta;

        InventarioEmpaquetado mov = new InventarioEmpaquetado();
        mov.setIdEmpaque(idEmpaque);
        mov.setCantidad(delta);
        mov.setFecha(LocalDateTime.now());
        mov.setMotivo(motivo);
        mov.setCantidadActual(nuevoStock);
        registrarMovimiento(mov);
        return nuevoStock;
    }

    /** Resta al stock y devuelve la existencia resultante. */
    public int restarInventarioBodega(int idEmpaque, int salida) throws SQLException {
        return moverInventarioSimple(idEmpaque, -salida, "SALIDA_DISTRIBUCION");
    }

    /** Suma al stock y devuelve la existencia resultante. */
    public int aumentarInventarioBodega(int idEmpaque, int entrada) throws SQLException {
        return moverInventarioSimple(idEmpaque, entrada, "INGRESO_BODEGA");
    }

    /** Ajuste genérico ±Δ (se usa en actualizarCantidad). */
    public int ajustarInventarioBodega(int idEmpaque, int delta) throws SQLException {
        String motivo = (delta < 0) ? "AJUSTE_SALIDA" : "AJUSTE_RETORNO";
        return moverInventarioSimple(idEmpaque, delta, motivo);
    }

    /* Wrapper antiguo de firma corta (compatibilidad) */
    private void registrarMovimientoSimple(int idEmpaque,
                                           int delta,
                                           String motivo) throws SQLException {
        moverInventarioSimple(idEmpaque, delta, motivo);
    }

    /* ============================================================ */
    /*  6. Helper de mapeo (para listarMovimientos)                 */
    /* ============================================================ */
    private InventarioEmpaquetado mapRow(ResultSet rs) throws SQLException {
        InventarioEmpaquetado ie = new InventarioEmpaquetado();
        ie.setIdInventario(rs.getInt("id_inventario"));
        ie.setIdEmpaque(rs.getInt("id_empaque"));
        ie.setCantidad(rs.getInt("cantidad"));
        ie.setMotivo(rs.getString("motivo"));
        ie.setCantidadActual(rs.getInt("cantidad_actual"));
        ie.setIdDistribucion((Integer) rs.getObject("id_distribucion"));
        ie.setIdRepartidor((Integer) rs.getObject("id_repartidor"));
        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) {
            var ldt = ts.toLocalDateTime();
            ie.setFecha(ldt);
            //ie.setFechaDate(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
        }
        return ie;
    }
}