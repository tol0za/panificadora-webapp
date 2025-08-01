package modelo;

import java.math.BigDecimal;

public class DetalleNotaVenta {
    private int idDetalle;
    private int idNota;
    private int idEmpaque;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String nombreEmpaque; // Si necesitas mostrar el nombre del empaque

    // --- Getters y Setters ---

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }

    public int getIdNota() { return idNota; }
    public void setIdNota(int idNota) { this.idNota = idNota; }

    public int getIdEmpaque() { return idEmpaque; }
    public void setIdEmpaque(int idEmpaque) { this.idEmpaque = idEmpaque; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getNombreEmpaque() { return nombreEmpaque; }
    public void setNombreEmpaque(String nombreEmpaque) { this.nombreEmpaque = nombreEmpaque; }
}
