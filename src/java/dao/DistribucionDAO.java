package dao;

import conexion.Conexion;
import dto.DistribucionFilaDTO;
import dto.DistribucionResumen;
import dto.InventarioDTO;
import modelo.Distribucion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO de la tabla <code>distribucion</code>.
 * Mantiene los métodos históricos y añade la lógica necesaria para
 * Salidas de Distribución y Notas de Venta.
 *
 * Regla: todos los movimientos de bodega se realizan a través del
 * SP `sp_mov_inventario_bodega` para evitar duplicidades y validar stock.
 */
public class DistribucionDAO {

    /* ----------------------------------------------------- */
    /*  Conexión                                             */
    /* ----------------------------------------------------- */
    private Connection getConn() throws SQLException {
        return Conexion.getConnection();
    }

    /* Soporte opcional para beginTx/commitTx/rollbackTx (compatibilidad) */
    private Connection txConn;

    public void beginTx() throws SQLException {
        if (txConn != null) return;      // ya abierta
        txConn = getConn();
        txConn.setAutoCommit(false);
    }
    public void commitTx() throws SQLException {
        if (txConn == null) return;
        try {
            txConn.commit();
        } finally {
            try { txConn.setAutoCommit(true); } catch (SQLException ignore) {}
            try { txConn.close(); } catch (SQLException ignore) {}
            txConn = null;
        }
    }
    public void rollbackTx() {
        if (txConn == null) return;
        try {
            txConn.rollback();
        } catch (SQLException ignore) {
        } finally {
            try { txConn.close(); } catch (SQLException ignore) {}
            txConn = null;
        }
    }

    /* ===================================================== */
    /*  MÉTODOS HISTÓRICOS                                   */
    /* ===================================================== */

    /** Devuelve TODAS las filas de un repartidor en una fecha concreta. */
    public List<Distribucion> buscarPorRepartidorYFecha(int idRepartidor,
                                                        LocalDate fecha)
            throws SQLException {

        String sql = """
            SELECT * FROM distribucion
             WHERE id_repartidor = ?
               AND DATE(fecha_distribucion) = ?
        """;

        List<Distribucion> lista = new ArrayList<>();
        try (Connection cn = getConn();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idRepartidor);
            ps.setDate(2, java.sql.Date.valueOf(fecha));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    /** Holder liviano para una fila de 'distribucion' (edición). */
    public static final class FilaDistrib {
        public final int idEmpaque;
        public final int idRepartidor;
        public final int cantidad;
        public FilaDistrib(int idEmpaque, int idRepartidor, int cantidad) {
            this.idEmpaque = idEmpaque;
            this.idRepartidor = idRepartidor;
            this.cantidad = cantidad;
        }
    }

    /** Obtiene datos básicos de una fila por su id (para flujos de edición). */
    public FilaDistrib obtenerDistribucionPorId(int idDistribucion) throws SQLException {
        final String sql = """
            SELECT id_empaque, id_repartidor, cantidad
              FROM distribucion
             WHERE id_distribucion = ?
        """;
        try (Connection cn = getConn();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDistribucion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new FilaDistrib(
                        rs.getInt("id_empaque"),
                        rs.getInt("id_repartidor"),
                        rs.getInt("cantidad")
                );
            }
        }
    }

    /** Obtiene la última salida registrada de un repartidor. */
    public Distribucion obtenerUltimaDistribucionPorRepartidor(int idRepartidor)
            throws SQLException {

        String sql = """
            SELECT * FROM distribucion
             WHERE id_repartidor = ?
             ORDER BY fecha_distribucion DESC
             LIMIT 1
        """;

        try (Connection cn = getConn();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /* ===================================================== */
    /*  NUEVO — Listar por fecha (DTO con nombres)           */
    /* ===================================================== */

    /** Lista completa de salidas de la fecha dada (con nombres de rep/empaque). */
    public List<DistribucionFilaDTO> listarPorFecha(LocalDate fecha) throws SQLException {

        String sql = """
            SELECT d.id_distribucion,
                   r.id_repartidor,
                   CONCAT(r.nombre_repartidor,' ',IFNULL(r.apellido_repartidor,'')) AS nom_rep,
                   e.id_empaque,
                   e.nombre_empaque,
                   d.cantidad,
                   d.fecha_distribucion
              FROM distribucion d
              JOIN repartidores     r ON r.id_repartidor = d.id_repartidor
              JOIN catalogo_empaque e ON e.id_empaque    = d.id_empaque
             WHERE d.fecha_distribucion >= ?
               AND d.fecha_distribucion <  ?
             ORDER BY d.id_distribucion
        """;

        List<DistribucionFilaDTO> lista = new ArrayList<>();
        try (Connection cn = getConn();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fecha.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DistribucionFilaDTO(
                            rs.getInt(1),         // id_distribucion
                            rs.getInt(2),         // id_repartidor
                            rs.getString(3),      // nombre repartidor
                            rs.getInt(4),         // id_empaque
                            rs.getString(5),      // nombre empaque
                            rs.getInt(6),         // cantidad
                            rs.getTimestamp(7).toLocalDateTime()
                    ));
                }
            }
        }
        return lista;
    }

    /* ===================================================== */
    /*  NUEVOS MÉTODOS (Notas de Venta)                      */
    /* ===================================================== */

    /** Repartidores que registraron al menos una salida ese día. */
   public List<DistribucionResumen> repartidoresConSalida(LocalDate fecha) throws SQLException {
    String sql = """
        SELECT MIN(d.id_distribucion) AS id_distribucion,
               r.id_repartidor,
               TRIM(CONCAT(r.nombre_repartidor,' ',IFNULL(r.apellido_repartidor,''))) AS nombre_repartidor
          FROM distribucion d
          JOIN repartidores r USING(id_repartidor)
         WHERE d.fecha_distribucion >= ?
           AND d.fecha_distribucion <  ?
         GROUP BY r.id_repartidor, r.nombre_repartidor, r.apellido_repartidor
         ORDER BY nombre_repartidor
    """;

    List<DistribucionResumen> lista = new ArrayList<>();
    try (Connection cn = getConn();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setTimestamp(1, Timestamp.valueOf(fecha.atStartOfDay()));
        ps.setTimestamp(2, Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new DistribucionResumen(
                        rs.getInt("id_distribucion"),
                        rs.getInt("id_repartidor"),
                        rs.getString("nombre_repartidor")   // ← viene “Nombre Apellido”
                ));
            }
        }
    }
    return lista;
}

    /** Inventario pendiente (>0) del repartidor en la fecha dada. */
    public List<InventarioDTO> inventarioPendiente(int idRepartidor,
                                                   LocalDate fecha)
            throws SQLException {

        String sql = """
            SELECT d.id_distribucion,
                   d.id_empaque,
                   ce.nombre_empaque,
                   ce.precio_unitario,
                   COALESCE(ir.cantidad_restante,
                            d.cantidad
                            - IFNULL(ir.cantidad_vendida,0)
                            - IFNULL(ir.cantidad_mermada,0)) AS restante
              FROM distribucion d
              JOIN catalogo_empaque ce ON ce.id_empaque = d.id_empaque
              LEFT JOIN inventario_repartidor ir
                     ON ir.id_repartidor = d.id_repartidor
                    AND ir.id_empaque    = d.id_empaque
             WHERE d.id_repartidor = ?
               AND d.fecha_distribucion >= ?
               AND d.fecha_distribucion <  ?
             HAVING restante > 0
        """;

        List<InventarioDTO> lista = new ArrayList<>();
        try (Connection cn = getConn();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idRepartidor);
            ps.setTimestamp(2, Timestamp.valueOf(fecha.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new InventarioDTO(
                            rs.getInt("id_distribucion"),
                            rs.getInt("id_empaque"),
                            rs.getString("nombre_empaque"),
                            rs.getDouble("precio_unitario"),
                            rs.getInt("restante")));
                }
            }
        }
        return lista;
    }

    /* ===================================================== */
    /*  NUEVOS MÉTODOS – Salidas / Inventario                */
    /* ===================================================== */

    /**
     * Inserta la salida completa (múltiples líneas) y ajusta inventarios.
     * Hace UN solo movimiento en bodega por línea vía SP (evita doble descuento).
     */
    public void crearSalida(int idRepartidor,
                            Map<Integer, Integer> lineas) throws Exception {

        String sqlIns = """
            INSERT INTO distribucion(id_repartidor,id_empaque,cantidad,fecha_distribucion)
            VALUES (?,?,?,NOW())
        """;

        try (Connection cn = getConn();
             PreparedStatement psIns = cn.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {

            cn.setAutoCommit(false);
            try {
                for (Map.Entry<Integer, Integer> e : lineas.entrySet()) {
                    int idEmp = e.getKey();
                    int cant  = e.getValue();
                    if (cant <= 0) continue;

                    // 1) Inserta la línea de salida
                    psIns.setInt(1, idRepartidor);
                    psIns.setInt(2, idEmp);
                    psIns.setInt(3, cant);
                    psIns.executeUpdate();

                    int idDistrib = 0;
                    try (ResultSet gk = psIns.getGeneratedKeys()) {
                        if (gk.next()) idDistrib = gk.getInt(1);
                    }

                    // 2) Bodega: único movimiento (delta negativo). El SP valida stock.
                    callSpMovBodega(cn, idEmp, -cant, "SALIDA_DISTRIBUCION", idRepartidor, idDistrib);

                    // 3) Inventario del repartidor (+cant)
                    upsertInventarioRepartidor(cn, idRepartidor, idEmp, +cant);
                }
                cn.commit();
            } catch (Exception ex) {
                try { cn.rollback(); } catch (SQLException ignore) {}
                throw ex;
            }
        }
    }

    /**
     * Ajusta la cantidad de una línea ya existente.
     * delta > 0 => se entregan piezas extra (bodega -delta; rep +delta)
     * delta < 0 => retorno (bodega +abs; rep -abs)
     */
    public void actualizarCantidad(int idDistribucion, int nuevaCantidad) throws Exception {

        String sel = """
            SELECT id_empaque, id_repartidor, cantidad
              FROM distribucion
             WHERE id_distribucion = ?
             FOR UPDATE
        """;
        String upd = "UPDATE distribucion SET cantidad=? WHERE id_distribucion=?";

        try (Connection cn = getConn();
             PreparedStatement psSel = cn.prepareStatement(sel);
             PreparedStatement psUpd = cn.prepareStatement(upd)) {

            cn.setAutoCommit(false);
            try {
                psSel.setInt(1, idDistribucion);

                int idEmp, idRep, anterior;
                try (ResultSet rs = psSel.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Distribución no encontrada");
                    idEmp    = rs.getInt("id_empaque");
                    idRep    = rs.getInt("id_repartidor");
                    anterior = rs.getInt("cantidad");
                }

                if (existeNotaVenta(idDistribucion))
                    throw new IllegalStateException("Ya existe nota de venta ligada a esta salida.");

                int delta = nuevaCantidad - anterior;
                if (delta != 0) {
                    if (delta > 0) {
                        callSpMovBodega(cn, idEmp, -delta, "AJUSTE_SALIDA",  idRep, idDistribucion);
                        upsertInventarioRepartidor(cn, idRep, idEmp, +delta);
                    } else {
                        int ret = -delta;
                        callSpMovBodega(cn, idEmp, +ret,  "AJUSTE_RETORNO", idRep, idDistribucion);
                        upsertInventarioRepartidor(cn, idRep, idEmp, -ret);
                    }
                }

                psUpd.setInt(1, nuevaCantidad);
                psUpd.setInt(2, idDistribucion);
                psUpd.executeUpdate();

                cn.commit();
            } catch (Exception ex) {
                try { cn.rollback(); } catch (SQLException ignore) {}
                throw ex;
            }
        }
    }

    /**
     * Elimina por completo una salida (todas las líneas con la misma fecha/hora exacta).
     * Devuelve a bodega cada línea y descuenta del inventario del repartidor.
     */
    public void eliminarSalida(int idRepartidor, LocalDateTime fechaHora) throws Exception {

        String sel = """
            SELECT id_distribucion, id_empaque, cantidad
              FROM distribucion
             WHERE id_repartidor = ?
               AND fecha_distribucion = ?
             FOR UPDATE
        """;
        String del = """
            DELETE FROM distribucion
             WHERE id_repartidor = ?
               AND fecha_distribucion = ?
        """;

        try (Connection cn = getConn();
             PreparedStatement psSel = cn.prepareStatement(sel);
             PreparedStatement psDel = cn.prepareStatement(del)) {

            cn.setAutoCommit(false);
            try {
                psSel.setInt(1, idRepartidor);
                psSel.setTimestamp(2, Timestamp.valueOf(fechaHora));

                List<int[]> filas = new ArrayList<>();
                try (ResultSet rs = psSel.executeQuery()) {
                    while (rs.next()) {
                        int idDist = rs.getInt("id_distribucion");
                        if (existeNotaVenta(idDist))
                            throw new IllegalStateException("Salida ligada a nota de venta.");
                        filas.add(new int[]{ rs.getInt("id_distribucion"),
                                             rs.getInt("id_empaque"),
                                             rs.getInt("cantidad") });
                    }
                }

                for (int[] f : filas) {
                    int idDist = f[0], idEmp = f[1], cant = f[2];
                    callSpMovBodega(cn, idEmp, +cant, "ENTRADA_RETORNO", idRepartidor, idDist);
                    upsertInventarioRepartidor(cn, idRepartidor, idEmp, -cant);
                }

                psDel.setInt(1, idRepartidor);
                psDel.setTimestamp(2, Timestamp.valueOf(fechaHora));
                psDel.executeUpdate();

                cn.commit();
            } catch (Exception ex) {
                try { cn.rollback(); } catch (SQLException ignore) {}
                throw ex;
            }
        }
    }

    /* ===================================================== */
    /*  Helpers                                              */
    /* ===================================================== */

    /** Llama al SP que registra un único movimiento en bodega (+/- delta). */
    private void callSpMovBodega(Connection cn,
                                 int idEmpaque,
                                 int delta,
                                 String motivo,
                                 Integer idRepartidor,
                                 Integer idDistribucion) throws SQLException {
        try (CallableStatement cs = cn.prepareCall("{ call sp_mov_inventario_bodega(?,?,?,?,?) }")) {
            cs.setInt(1, idEmpaque);
            cs.setInt(2, delta);
            cs.setString(3, motivo);
            if (idRepartidor == null) cs.setNull(4, Types.INTEGER); else cs.setInt(4, idRepartidor);
            if (idDistribucion == null) cs.setNull(5, Types.INTEGER); else cs.setInt(5, idDistribucion);
            cs.execute();
        }
    }

    /** Inserta/actualiza inventario del repartidor (acumulado distribuido y restante). */
    private void upsertInventarioRepartidor(Connection cn,
                                            int idRepartidor,
                                            int idEmpaque,
                                            int delta) throws SQLException {
        String sql = """
            INSERT INTO inventario_repartidor(id_repartidor, id_empaque, cantidad_distribuida, cantidad_restante)
            VALUES (?,?,?,?)
            ON DUPLICATE KEY UPDATE
                cantidad_distribuida = cantidad_distribuida + VALUES(cantidad_distribuida),
                cantidad_restante    = GREATEST(0, cantidad_restante + VALUES(cantidad_restante))
        """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setInt(2, idEmpaque);
            ps.setInt(3, Math.max(0, delta)); // solo suma al acumulado cuando delta > 0
            ps.setInt(4, delta);              // restante puede subir o bajar
            ps.executeUpdate();
        }
    }

    private boolean existeNotaVenta(int idDistrib) throws SQLException {
        String q = "SELECT 1 FROM detalle_nota_venta WHERE id_distribucion=? LIMIT 1";
        try (Connection cn = getConn();
             PreparedStatement ps = cn.prepareStatement(q)) {
            ps.setInt(1, idDistrib);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Convierte fila ResultSet -> Distribucion. */
    private Distribucion mapRow(ResultSet rs) throws SQLException {
        Distribucion d = new Distribucion();
        d.setIdDistribucion(rs.getInt("id_distribucion"));
        d.setIdRepartidor(rs.getInt("id_repartidor"));
        d.setIdEmpaque(rs.getInt("id_empaque"));
        d.setCantidad(rs.getInt("cantidad"));
        Timestamp ts = rs.getTimestamp("fecha_distribucion");
        if (ts != null) d.setFechaDistribucion(ts.toLocalDateTime());
        return d;
    }

    /* ===================================================== */
    /*  Detalle de una salida (DTO)                          */
    /* ===================================================== */

    /** Líneas de una salida identificada por repartidor + fecha/hora exacta. */
    public List<DistribucionFilaDTO> buscarPorRepartidorYFechaHora(int idRep,
                                                                   LocalDateTime fechaHora)
            throws SQLException {

        String sql = """
            SELECT d.id_distribucion,
                   r.id_repartidor,
                   CONCAT(r.nombre_repartidor,' ',IFNULL(r.apellido_repartidor,'')),
                   e.id_empaque,
                   e.nombre_empaque,
                   d.cantidad,
                   d.fecha_distribucion
              FROM distribucion d
              JOIN repartidores     r ON r.id_repartidor = d.id_repartidor
              JOIN catalogo_empaque e ON e.id_empaque    = d.id_empaque
             WHERE d.id_repartidor     = ?
               AND d.fecha_distribucion = ?
             ORDER BY e.nombre_empaque
        """;

        List<DistribucionFilaDTO> lista = new ArrayList<>();
        try (Connection cn = getConn();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idRep);
            ps.setTimestamp(2, Timestamp.valueOf(fechaHora));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DistribucionFilaDTO(
                            rs.getInt(1), rs.getInt(2), rs.getString(3),
                            rs.getInt(4), rs.getString(5),
                            rs.getInt(6), rs.getTimestamp(7).toLocalDateTime()));
                }
            }
        }
        return lista;
    }
}
