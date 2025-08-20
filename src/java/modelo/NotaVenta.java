package modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo que representa el encabezado de una nota de venta.
 * Se integra al resto del sistema sin afectar clases existentes.
 */
public class NotaVenta {

    private int idNotaVenta;      // PK autoincremental
    private int folio;            // ÚNICO, ingresado manualmente
    private int idRepartidor;     // FK repartidores.id_repartidor
    private int idTienda;         // FK tiendas.id_tienda
    private LocalDateTime fechaNota; // TIMESTAMP de la venta
    private double total;         // Calculado como suma de subtotales (solo vendidas)
private String nombreRepartidor;
private String nombreTienda;
    // --- Relación uno‑a‑muchos con el detalle (opcional en backoffice) ---
    private List<DetalleNotaVenta> detalles;

    // ---------------------------------------------------------------------
    // Getters & Setters                                                    
    // ---------------------------------------------------------------------
public String getNombreRepartidor() { return nombreRepartidor; }
public void setNombreRepartidor(String s) { this.nombreRepartidor = s; }

// Alias para compatibilidad con JSP antiguas:
public java.sql.Timestamp getFechaHora() {
    return getFechaNota() == null ? null : java.sql.Timestamp.valueOf(getFechaNota());
}
    public int getIdNotaVenta() {
        return idNotaVenta;
    }

    public void setIdNotaVenta(int idNotaVenta) {
        this.idNotaVenta = idNotaVenta;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public int getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(int idRepartidor) {
        this.idRepartidor = idRepartidor;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public LocalDateTime getFechaNota() {
        return fechaNota;
    }

    public void setFechaNota(LocalDateTime fechaNota) {
        this.fechaNota = fechaNota;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<DetalleNotaVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleNotaVenta> detalles) {
        this.detalles = detalles;
    }
    public String getNombreTienda() { return nombreTienda; }
public void setNombreTienda(String nombreTienda) { this.nombreTienda = nombreTienda; }
public String getFechaNotaStr() {
    return fechaNota != null ? fechaNota.toString().replace('T',' ') : "";
}

public int getIdNota() {              // ← alias para JSP antiguos
    return idNotaVenta;
}
}
