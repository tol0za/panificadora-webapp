package dao;

import modelo.Distribucion;
import conexion.Conexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DistribucionDAO {

    public List<Distribucion> buscarPorRepartidorYFecha(int idRepartidor, LocalDate fecha) throws SQLException {
        List<Distribucion> lista = new ArrayList<>();
        String sql = "SELECT * FROM distribucion WHERE id_repartidor = ? AND DATE(fecha_distribucion) = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRepartidor);
            stmt.setDate(2, java.sql.Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Distribucion d = new Distribucion();
                    d.setIdDistribucion(rs.getInt("id_distribucion"));
                    d.setIdRepartidor(rs.getInt("id_repartidor"));
                    d.setIdEmpaque(rs.getInt("id_empaque"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setFechaDistribucion(rs.getTimestamp("fecha_distribucion").toLocalDateTime());
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    // 🚨 NUEVO MÉTODO NECESARIO PARA NOTAS DE VENTA
 public Distribucion obtenerUltimaDistribucionPorRepartidor(int idRepartidor) throws SQLException {
    System.out.println("🔍 Buscando última distribución para repartidor ID: " + idRepartidor);

    String sql = "SELECT * FROM distribucion WHERE id_repartidor = ? ORDER BY fecha_distribucion DESC LIMIT 1";
    try (Connection conn = Conexion.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idRepartidor);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Distribucion d = new Distribucion();
                d.setIdDistribucion(rs.getInt("id_distribucion"));
                d.setIdRepartidor(rs.getInt("id_repartidor"));
                d.setIdEmpaque(rs.getInt("id_empaque"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setFechaDistribucion(rs.getTimestamp("fecha_distribucion").toLocalDateTime());

                System.out.println("✅ Distribución encontrada: ID " + d.getIdDistribucion());
                return d;
            } else {
                System.out.println("⚠️ No se encontró distribución para este repartidor.");
            }
        }
    }
    return null;
}
}
