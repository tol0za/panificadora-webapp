package dto;

/**
 * DTO que representa el inventario pendiente de un repartidor
 * para un empaque específico en la salida del día.
 */
public class InventarioDTO {

    private int idDistribucion;
    private int idEmpaque;
    private String nombre;
    private double precio;
    private int restante;

    public InventarioDTO() { }

    public InventarioDTO(int idDistribucion, int idEmpaque,
                         String nombre, double precio, int restante) {
        this.idDistribucion = idDistribucion;
        this.idEmpaque = idEmpaque;
        this.nombre = nombre;
        this.precio = precio;
        this.restante = restante;
    }

    /* ------------ getters & setters ------------ */
    public int getIdDistribucion() { return idDistribucion; }
    public void setIdDistribucion(int idDistribucion) { this.idDistribucion = idDistribucion; }

    public int getIdEmpaque() { return idEmpaque; }
    public void setIdEmpaque(int idEmpaque) { this.idEmpaque = idEmpaque; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getRestante() { return restante; }
    public void setRestante(int restante) { this.restante = restante; }
}
