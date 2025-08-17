package dao;

import conexion.Conexion;
import modelo.InventarioEmpaquetado;
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
 */
public class DistribucionDAO {

    /* ----------------------------------------------------- */
    /*  Utilidad                                             */
    /* ----------------------------------------------------- */
    private Connection getConn() throws SQLException {
        return Conexion.getConnection();
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

    /**
     * Devuelve la lista completa de salidas de la fecha dada,
     * incluyendo nombres de repartidor y empaque.
     */
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
    public List<DistribucionResumen> repartidoresConSalida(LocalDate fecha)
            throws SQLException {

        String sql = """
            SELECT MIN(d.id_distribucion) AS id_distribucion,
                   r.id_repartidor,
                   r.nombre_repartidor
              FROM distribucion d
              JOIN repartidores r USING(id_repartidor)
             WHERE d.fecha_distribucion >= ?
               AND d.fecha_distribucion <  ?
             GROUP BY r.id_repartidor, r.nombre_repartidor
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
                            rs.getString("nombre_repartidor")));
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

    /** Inserta la salida (múltiples líneas) y ajusta inventario. */
  /* ===============================================================
 *  Inserta la salida completa y ajusta inventarios
 * ===============================================================*/

/* ==============================================================
 *  DistribucionDAO  –  crearSalida SIN duplicar movimientos
 * ============================================================== */
public void crearSalida(int idRepartidor,
                        Map<Integer,Integer> lineas) throws Exception {

    String sqlIns = """
        INSERT INTO distribucion(id_repartidor,id_empaque,cantidad,fecha_distribucion)
        VALUES (?,?,?,NOW())
    """;

    try (Connection cn = getConn();
         PreparedStatement ps = cn.prepareStatement(sqlIns)) {

        InventarioEmpaquetadoDAO invDao = new InventarioEmpaquetadoDAO();
        InventarioRepartidorDAO  repDao = new InventarioRepartidorDAO();

        for (var e : lineas.entrySet()) {
            int idEmp = e.getKey();
            int cant  = e.getValue();

            /* 1) ‑‑‑‑‑ Stock global de bodega (resta) */
            int nuevoStock = invDao.restarInventarioBodega(idEmp, cant);

            /* 2) ‑‑‑‑‑ Inventario del repartidor  (+)  ► crea movimiento */
            repDao.sumarInventarioRepartidor(idRepartidor, idEmp, cant, nuevoStock);

            /* 3) ‑‑‑‑‑ Registro principal en tabla distribucion */
            ps.setInt(1, idRepartidor);
            ps.setInt(2, idEmp);
            ps.setInt(3, cant);
            ps.addBatch();
        }
        ps.executeBatch();
    }
}
/* =========================================================
 *  Ajustar cantidad de una línea ya registrada
 * =========================================================*/
public void actualizarCantidad(int idDistribucion, int nuevaCantidad) throws Exception {

    String sel = """
        SELECT id_empaque, id_repartidor, cantidad
          FROM distribucion
         WHERE id_distribucion = ?
    """;
    String upd = """
        UPDATE distribucion SET cantidad = ?
         WHERE id_distribucion = ?
    """;

    try (Connection cn = getConn();
         PreparedStatement psSel = cn.prepareStatement(sel);
         PreparedStatement psUpd = cn.prepareStatement(upd)) {

        psSel.setInt(1, idDistribucion);

        int idEmp, idRep, anterior;
        try (ResultSet rs = psSel.executeQuery()) {
            if (!rs.next()) throw new SQLException("Distribución no encontrada");
            idEmp    = rs.getInt("id_empaque");
            idRep    = rs.getInt("id_repartidor");
            anterior = rs.getInt("cantidad");
        }

        if (existeNotaVenta(idDistribucion))
            throw new IllegalStateException("Ya existe nota de venta.");

        int delta = nuevaCantidad - anterior;
        if (delta != 0) {
            InventarioEmpaquetadoDAO invDao = new InventarioEmpaquetadoDAO();
            InventarioRepartidorDAO  repDao = new InventarioRepartidorDAO();

            /* ---------- movimiento en bodega y reparto ---------- */
            int nuevoStock;
            String motivo;
            if (delta > 0) {                                   // se entregan piezas extra
                nuevoStock = invDao.restarInventarioBodega(idEmp, delta);
                repDao.sumarInventarioRepartidor(idRep, idEmp, delta);
                motivo = "AJUSTE_SALIDA";
            } else {                                           // se devuelven piezas
                nuevoStock = invDao.aumentarInventarioBodega(idEmp, -delta);
                repDao.restarInventarioRepartidor(idRep, idEmp, -delta);
                motivo = "AJUSTE_RETORNO";
            }

            /* ---------- registrar movimiento ---------- */
            InventarioEmpaquetado mov = new InventarioEmpaquetado();
            mov.setIdEmpaque(idEmp);
            mov.setIdRepartidor(idRep);
            mov.setIdDistribucion(idDistribucion);
            mov.setCantidad(delta);                            // + / –
            mov.setFecha(java.time.LocalDateTime.now());
            mov.setMotivo(motivo);
            mov.setCantidadActual(nuevoStock);
            invDao.registrarMovimiento(mov);
        }

        /* actualizar tabla distribucion */
        psUpd.setInt(1, nuevaCantidad);
        psUpd.setInt(2, idDistribucion);
        psUpd.executeUpdate();
    }
}

/* =========================================================
 *  Eliminar por completo una salida (mismo repartidor-fecha)
 * =========================================================*/
public void eliminarSalida(int idRepartidor, java.time.LocalDateTime fechaHora) throws Exception {

    String sel = """
        SELECT id_distribucion, id_empaque, cantidad
          FROM distribucion
         WHERE id_repartidor = ?
           AND fecha_distribucion = ?
    """;
    String del = """
        DELETE FROM distribucion
         WHERE id_repartidor = ?
           AND fecha_distribucion = ?
    """;

    try (Connection cn = getConn();
         PreparedStatement psSel = cn.prepareStatement(sel);
         PreparedStatement psDel = cn.prepareStatement(del)) {

        psSel.setInt(1, idRepartidor);
        psSel.setTimestamp(2, java.sql.Timestamp.valueOf(fechaHora));

        InventarioEmpaquetadoDAO invDao = new InventarioEmpaquetadoDAO();
        InventarioRepartidorDAO  repDao = new InventarioRepartidorDAO();

        try (ResultSet rs = psSel.executeQuery()) {
            while (rs.next()) {
                int idDist = rs.getInt("id_distribucion");
                if (existeNotaVenta(idDist))
                    throw new IllegalStateException("Salida ligada a nota de venta.");

                int idEmp = rs.getInt("id_empaque");
                int cant  = rs.getInt("cantidad");

                /* ---------- devolver a bodega ---------- */
                int nuevoStock = invDao.aumentarInventarioBodega(idEmp, cant);    // (+)
                repDao.restarInventarioRepartidor(idRepartidor, idEmp, cant);     // (–)

                /* ---------- registrar retorno ---------- */
                InventarioEmpaquetado mov = new InventarioEmpaquetado();
                mov.setIdEmpaque(idEmp);
                mov.setIdRepartidor(idRepartidor);
                mov.setIdDistribucion(idDist);
                mov.setCantidad(cant);                           // retorno ⇒ positivo
                mov.setFecha(java.time.LocalDateTime.now());
                mov.setMotivo("ENTRADA_RETORNO");
                mov.setCantidadActual(nuevoStock);
                invDao.registrarMovimiento(mov);
            }
        }

        /* eliminar las filas de distribucion */
        psDel.setInt(1, idRepartidor);
        psDel.setTimestamp(2, java.sql.Timestamp.valueOf(fechaHora));
        psDel.executeUpdate();
    }
}

    /* ===================================================== */
    /*  Helpers                                              */
    /* ===================================================== */

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

    /**
     * Líneas de una salida identificada por repartidor + fecha/hora exacta.
     */
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