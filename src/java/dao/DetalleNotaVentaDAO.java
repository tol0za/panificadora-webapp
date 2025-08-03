package dao;

import modelo.DetalleNotaVenta;
import conexion.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleNotaVentaDAO {
    private Connection conn;

    public DetalleNotaVentaDAO() throws SQLException {
        conn = Conexion.getConnection();
    }

    // Método principal para obtener detalles con nombre del empaque incluido
    public List<DetalleNotaVenta> obtenerDetallesPorNota(int idNota) throws SQLException {
        List<DetalleNotaVenta> detalles = new ArrayList<>();
        String sql = "SELECT dnv.id_detalle, dnv.id_nota, dnv.id_empaque, e.nombre_empaque, " +
                     "dnv.cantidad_vendida, dnv.precio_unitario, dnv.merma, " +
                     "(dnv.cantidad_vendida * dnv.precio_unitario) AS total_linea " +
                     "FROM detalle_nota_venta dnv " +
                     "JOIN catalogo_empaque e ON dnv.id_empaque = e.id_empaque " +
                     "WHERE dnv.id_nota = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DetalleNotaVenta detalle = new DetalleNotaVenta();
                    detalle.setIdDetalle(rs.getInt("id_detalle"));
                    detalle.setIdNotaVenta(rs.getInt("id_nota"));
                    detalle.setIdEmpaque(rs.getInt("id_empaque"));
                    detalle.setNombreEmpaque(rs.getString("nombre_empaque"));
                    detalle.setCantidadVendida(rs.getInt("cantidad_vendida"));
                    detalle.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    detalle.setMerma(rs.getInt("merma"));
                    detalle.setTotalLinea(rs.getDouble("total_linea"));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    // Método alternativo simple sin nombre_empaque (si se requiere sin JOIN)
    public List<DetalleNotaVenta> buscarPorNota(int idNota) throws SQLException {
        List<DetalleNotaVenta> lista = new ArrayList<>();
        String sql = "SELECT id_empaque, cantidad_vendida, merma, precio_unitario, " +
                     "(cantidad_vendida * precio_unitario) AS total_linea " +
                     "FROM detalle_nota_venta WHERE id_nota = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DetalleNotaVenta detalle = new DetalleNotaVenta();
                    detalle.setIdEmpaque(rs.getInt("id_empaque"));
                    detalle.setCantidadVendida(rs.getInt("cantidad_vendida"));
                    detalle.setMerma(rs.getInt("merma"));
                    detalle.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    detalle.setTotalLinea(rs.getDouble("total_linea"));
                    lista.add(detalle);
                }
            }
        }
        return lista;
    }
}