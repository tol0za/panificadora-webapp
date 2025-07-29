package modelo;

import java.time.LocalDateTime;
import java.util.Date;

public class Salida {
    private int idDistribucion;
    private int idRepartidor;
    private int idEmpaque;
    private int cantidad;
    private LocalDateTime fechaDistribucion;

    private Date fechaDistribucionDate; // para JSP

    // Constructor vacío necesario para DAO y frameworks
    public Salida() {
    }

    public Salida(int idDistribucion, int idRepartidor, int idEmpaque, int cantidad, LocalDateTime fechaDistribucion) {
        this.idDistribucion = idDistribucion;
        this.idRepartidor = idRepartidor;
        this.idEmpaque = idEmpaque;
        this.cantidad = cantidad;
        this.fechaDistribucion = fechaDistribucion;
    }

    // Getters y setters

    public int getIdDistribucion() {
        return idDistribucion;
    }

    public void setIdDistribucion(int idDistribucion) {
        this.idDistribucion = idDistribucion;
    }

    public int getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(int idRepartidor) {
        this.idRepartidor = idRepartidor;
    }

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaDistribucion() {
        return fechaDistribucion;
    }

    public void setFechaDistribucion(LocalDateTime fechaDistribucion) {
        this.fechaDistribucion = fechaDistribucion;
    }

    public Date getFechaDistribucionDate() {
        return fechaDistribucionDate;
    }

    public void setFechaDistribucionDate(Date fechaDistribucionDate) {
        this.fechaDistribucionDate = fechaDistribucionDate;
    }
}
