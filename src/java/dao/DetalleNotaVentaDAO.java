package dao;

import modelo.DetalleNotaVenta;
import conexion.Conexion;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class DetalleNotaVentaDAO {

    public void insertar(DetalleNotaVenta det, int idNota) throws SQLException {
        String sql = "INSERT INTO detalle_nota_venta (id_nota, id_empaque, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            ps.setInt(2, det.getIdEmpaque());
            ps.setInt(3, det.getCantidad());
            ps.setBigDecimal(4, det.getPrecioUnitario());
            ps.setBigDecimal(5, det.getSubtotal());
            ps.executeUpdate();
        }
    }

    public void insertarVarios(List<DetalleNotaVenta> detalles, int idNota) throws SQLException {
        for (DetalleNotaVenta det : detalles) {
            insertar(det, idNota);
        }
    }

    public List<DetalleNotaVenta> listarPorNota(int idNota) throws SQLException {
        List<DetalleNotaVenta> lista = new ArrayList<>();
        String sql = "SELECT d.*, e.nombre_empaque FROM detalle_nota_venta d " +
                     "LEFT JOIN catalogo_empaque e ON d.id_empaque = e.id_empaque " +
                     "WHERE d.id_nota = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleNotaVenta det = new DetalleNotaVenta();
                    det.setIdDetalle(rs.getInt("id_detalle"));
                    det.setIdNota(rs.getInt("id_nota"));
                    det.setIdEmpaque(rs.getInt("id_empaque"));
                    det.setCantidad(rs.getInt("cantidad"));
                    det.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    det.setSubtotal(rs.getBigDecimal("subtotal"));
                    det.setNombreEmpaque(rs.getString("nombre_empaque"));
                    lista.add(det);
                }
            }
        }
        return lista;
    }

    public void eliminarPorNota(int idNota) throws SQLException {
        String sql = "DELETE FROM detalle_nota_venta WHERE id_nota = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            ps.executeUpdate();
        }
    }

    // Si necesitas eliminar un solo detalle:
    public void eliminarPorId(int idDetalle) throws SQLException {
        String sql = "DELETE FROM detalle_nota_venta WHERE id_detalle = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            ps.executeUpdate();
        }
    }
}
