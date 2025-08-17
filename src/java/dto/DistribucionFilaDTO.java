package dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Fila de la vista “Salidas de Distribución”.
 * Contiene los datos ya “enriquecidos” con nombres de repartidor y empaque.
 */
public class DistribucionFilaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /* ---- atributos ---- */
    private int idDistribucion;
    private int idRepartidor;
    private String nombreRepartidorCompleto;
    private int idEmpaque;
    private String nombreEmpaque;
    private int cantidad;
    private LocalDateTime fechaDistribucion;

    /* ---- constructores ---- */
    public DistribucionFilaDTO() { }

    public DistribucionFilaDTO(int idDistribucion, int idRepartidor,
                               String nombreRepCompleto,
                               int idEmpaque, String nombreEmpaque,
                               int cantidad, LocalDateTime fechaDistribucion) {
        this.idDistribucion           = idDistribucion;
        this.idRepartidor             = idRepartidor;
        this.nombreRepartidorCompleto = nombreRepCompleto;
        this.idEmpaque                = idEmpaque;
        this.nombreEmpaque            = nombreEmpaque;
        this.cantidad                 = cantidad;
        this.fechaDistribucion        = fechaDistribucion;
    }

    /* ---- getters & setters ---- */
    public int getIdDistribucion() { return idDistribucion; }
    public void setIdDistribucion(int idDistribucion) { this.idDistribucion = idDistribucion; }

    public int getIdRepartidor() { return idRepartidor; }
    public void setIdRepartidor(int idRepartidor) { this.idRepartidor = idRepartidor; }

    public String getNombreRepartidorCompleto() { return nombreRepartidorCompleto; }
    public void setNombreRepartidorCompleto(String n) { this.nombreRepartidorCompleto = n; }

    public int getIdEmpaque() { return idEmpaque; }
    public void setIdEmpaque(int idEmpaque) { this.idEmpaque = idEmpaque; }

    public String getNombreEmpaque() { return nombreEmpaque; }
    public void setNombreEmpaque(String nombreEmpaque) { this.nombreEmpaque = nombreEmpaque; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFechaDistribucion() { return fechaDistribucion; }
    public void setFechaDistribucion(LocalDateTime f) { this.fechaDistribucion = f; }

    /* ---- helper para el JSP (fecha formateada) ---- */
    public String getFechaFmt() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                .format(fechaDistribucion);
    }

    /* ---- toString ---- */
    @Override
    public String toString() {
        return "DistribucionFilaDTO{" +
               "idDistribucion=" + idDistribucion +
               ", idRepartidor=" + idRepartidor +
               ", nombreRepartidorCompleto='" + nombreRepartidorCompleto + '\'' +
               ", idEmpaque=" + idEmpaque +
               ", nombreEmpaque='" + nombreEmpaque + '\'' +
               ", cantidad=" + cantidad +
               ", fechaDistribucion=" + fechaDistribucion +
               '}';
    }
}
