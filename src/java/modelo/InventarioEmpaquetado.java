package modelo;

import java.time.LocalDateTime;
import java.util.Date;

public class InventarioEmpaquetado {

    private int idInventario;
    private int idDistribucion; // NUEVO
    private int idEmpaque;
    private String nombreEmpaque;
    private int cantidad;
    private LocalDateTime fecha;
    private String motivo;
    private int cantidadActual;
    private Date fechaDate;

    public InventarioEmpaquetado() {}

    public InventarioEmpaquetado(int idInventario, int idDistribucion, int idEmpaque, String nombreEmpaque, int cantidad,
                                 LocalDateTime fecha, String motivo, int cantidadActual) {
        this.idInventario = idInventario;
        this.idDistribucion = idDistribucion;
        this.idEmpaque = idEmpaque;
        this.nombreEmpaque = nombreEmpaque;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.motivo = motivo;
        this.cantidadActual = cantidadActual;
    }

    public int getIdInventario() { return idInventario; }
    public void setIdInventario(int idInventario) { this.idInventario = idInventario; }

    public int getIdDistribucion() { return idDistribucion; }
    public void setIdDistribucion(int idDistribucion) { this.idDistribucion = idDistribucion; }

    public int getIdEmpaque() { return idEmpaque; }
    public void setIdEmpaque(int idEmpaque) { this.idEmpaque = idEmpaque; }

    public String getNombreEmpaque() { return nombreEmpaque; }
    public void setNombreEmpaque(String nombreEmpaque) { this.nombreEmpaque = nombreEmpaque; }

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
