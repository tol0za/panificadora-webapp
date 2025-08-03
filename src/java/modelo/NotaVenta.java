package modelo;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class NotaVenta {
    private int idNotaVenta;
    private String folio;
    private LocalDateTime fecha;
    private int idDistribucion;
    private int idTienda;
    private String nombreTienda;
    private int idRepartidor;
    private String nombreRepartidor;
    private double totalNota;
    private List<DetalleNotaVenta> detalles;

    // Getters y Setters
    public int getIdNotaVenta() {
        return idNotaVenta;
    }

    public void setIdNotaVenta(int idNotaVenta) {
        this.idNotaVenta = idNotaVenta;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getIdDistribucion() {
        return idDistribucion;
    }

    public void setIdDistribucion(int idDistribucion) {
        this.idDistribucion = idDistribucion;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }

    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }

    public int getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(int idRepartidor) {
        this.idRepartidor = idRepartidor;
    }

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }

    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }

    public double getTotalNota() {
        return totalNota;
    }

    public void setTotalNota(double totalNota) {
        this.totalNota = totalNota;
    }

    public List<DetalleNotaVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleNotaVenta> detalles) {
        this.detalles = detalles;
    }

    // ✅ Conversión de LocalDateTime para JSTL
    public Date getFechaAsDate() {
        return java.util.Date.from(fecha.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }
    public String getFechaFormateada() {
        if (fecha != null) {
            return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }
        return "";
    }
}