package dto;

/**
 * DTO para mostrar en la tabla de “Repartidores con salida hoy”.
 */
public class DistribucionResumen {

    private int idDistribucion;
    private int idRepartidor;
    private String nombreRepartidor;

    public DistribucionResumen() { }

    public DistribucionResumen(int idDistribucion,
                               int idRepartidor,
                               String nombreRepartidor) {
        this.idDistribucion = idDistribucion;
        this.idRepartidor = idRepartidor;
        this.nombreRepartidor = nombreRepartidor;
    }

    /* ------------ getters & setters ------------ */
    public int getIdDistribucion() { return idDistribucion; }
    public void setIdDistribucion(int idDistribucion) { this.idDistribucion = idDistribucion; }

    public int getIdRepartidor() { return idRepartidor; }
    public void setIdRepartidor(int idRepartidor) { this.idRepartidor = idRepartidor; }

    public String getNombreRepartidor() { return nombreRepartidor; }
    public void setNombreRepartidor(String nombreRepartidor) { this.nombreRepartidor = nombreRepartidor; }
}
