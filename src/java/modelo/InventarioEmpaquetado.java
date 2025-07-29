package modelo;

import java.time.LocalDateTime;
import java.util.Date;

public class InventarioEmpaquetado {
    private int idInventario;
    private int idEmpaque;
    private int cantidad;
    private LocalDateTime fecha;
    private String motivo;
    private int cantidadActual;

    private Date fechaDate;  // <-- Agregado para JSP

    public InventarioEmpaquetado() {}

    public InventarioEmpaquetado(int idInventario, int idEmpaque, int cantidad, LocalDateTime fecha,
                                String motivo, int cantidadActual) {
        this.idInventario = idInventario;
        this.idEmpaque = idEmpaque;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.motivo = motivo;
        this.cantidadActual = cantidadActual;
    }

    public int getIdInventario() { return idInventario; }
    public void setIdInventario(int idInventario) { this.idInventario = idInventario; }

    public int getIdEmpaque() { return idEmpaque; }
    public void setIdEmpaque(int idEmpaque) { this.idEmpaque = idEmpaque; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public int getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(int cantidadActual) { this.cantidadActual = cantidadActual; }

    public Date getFechaDate() { return fechaDate; }
    public void setFechaDate(Date fechaDate) { this.fechaDate = fechaDate; }
}