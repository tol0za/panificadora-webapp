package modelo;

public class DistribucionResumen {
    private int idDistribucion;
    private int idRepartidor;
    private String nombreRepartidor;
    private int totalEmpaques;
    private int totalPiezas;
    private String fecha;

    public DistribucionResumen() {}

    public DistribucionResumen(int idDistribucion, int idRepartidor, String nombreRepartidor) {
        this.idDistribucion = idDistribucion;
        this.idRepartidor = idRepartidor;
        this.nombreRepartidor = nombreRepartidor;
    }

    // Getters y Setters
    public int getIdDistribucion() { return idDistribucion; }
    public void setIdDistribucion(int idDistribucion) { this.idDistribucion = idDistribucion; }

    public int getIdRepartidor() { return idRepartidor; }
    public void setIdRepartidor(int idRepartidor) { this.idRepartidor = idRepartidor; }

    public String getNombreRepartidor() { return nombreRepartidor; }
    public void setNombreRepartidor(String nombreRepartidor) { this.nombreRepartidor = nombreRepartidor; }

    public int getTotalEmpaques() { return totalEmpaques; }
    public void setTotalEmpaques(int totalEmpaques) { this.totalEmpaques = totalEmpaques; }

    public int getTotalPiezas() { return totalPiezas; }
    public void setTotalPiezas(int totalPiezas) { this.totalPiezas = totalPiezas; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
