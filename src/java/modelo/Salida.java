package modelo;

import java.time.LocalDateTime;
import java.util.Date;

public class Salida {
    private int idDistribucion;
    private int idRepartidor;
    private int idEmpaque;
    private int cantidad;
    private LocalDateTime fechaDistribucion;
    private Date fechaDistribucionDate; // Para JSTL
    // Extras para listado
    private String nombreRepartidor;
    private String apellidoRepartidor;
    private String nombreEmpaque;

    // Getters y Setters
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

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }
    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }

    public String getApellidoRepartidor() {
        return apellidoRepartidor;
    }
    public void setApellidoRepartidor(String apellidoRepartidor) {
        this.apellidoRepartidor = apellidoRepartidor;
    }

    public String getNombreEmpaque() {
        return nombreEmpaque;
    }
    public void setNombreEmpaque(String nombreEmpaque) {
        this.nombreEmpaque = nombreEmpaque;
    }
}
