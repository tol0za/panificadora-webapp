package modelo;

/**
 * Modelo para las líneas de una nota de venta.
 * Representa la relación entre la nota y los paquetes distribuidos.
 */
public class DetalleNotaVenta {

    private int idDetalle;        // PK autoincremental
    private int idNota;           // FK notas_venta.id_nota
    private int idDistribucion;   // FK distribucion.id_distribucion (para rastrear la salida)
    private int idEmpaque;        // FK catalogo_empaque.id_empaque

    private int cantidadVendida;  // piezas vendidas
    private int merma;            // piezas merma
    private double precioUnitario;// precio referenciado de catalogo_empaque
    private double totalLinea;    // calculado (cantidadVendida * precioUnitario)

    // ---------------- Getters & Setters ------------------
    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdNota() {
        return idNota;
    }

    public void setIdNota(int idNota) {
        this.idNota = idNota;
    }

    public int getIdDistribucion() {
        return idDistribucion;
    }

    public void setIdDistribucion(int idDistribucion) {
        this.idDistribucion = idDistribucion;
    }

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public int getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(int cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public int getMerma() {
        return merma;
    }

    public void setMerma(int merma) {
        this.merma = merma;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getTotalLinea() {
        return totalLinea;
    }

    public void setTotalLinea(double totalLinea) {
        this.totalLinea = totalLinea;
    }
}
