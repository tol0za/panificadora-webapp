package modelo;

import java.time.LocalDateTime;

public class Distribucion {
    private int idDistribucion;
    private int idRepartidor;
    private int idEmpaque;
    private int cantidad;
    private LocalDateTime fechaDistribucion;

    // Campo opcional si planeas mostrar el nombre del repartidor en vistas directamente
    private String nombreRepartidor;

    public Distribucion() {}

    public Distribucion(int idDistribucion, int idRepartidor, int idEmpaque, int cantidad, LocalDateTime fechaDistribucion) {
        this.idDistribucion = idDistribucion;
        this.idRepartidor = idRepartidor;
        this.idEmpaque = idEmpaque;
        this.cantidad = cantidad;
        this.fechaDistribucion = fechaDistribucion;
    }

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

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }

    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }
}
