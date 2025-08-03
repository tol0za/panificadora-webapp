package modelo;

import java.math.BigDecimal;

public class CatalogoEmpaque {
    private int id_empaque;
    private String nombre_empaque;
    private BigDecimal precio_unitario;
    private int stock;

    public int getIdEmpaque() {
        return id_empaque;
    }

    public void setIdEmpaque(int id_empaque) {
        this.id_empaque = id_empaque;
    }

    public String getNombreEmpaque() {
        return nombre_empaque;
    }

    public void setNombreEmpaque(String nombre_empaque) {
        this.nombre_empaque = nombre_empaque;
    }

    public BigDecimal getPrecioUnitario() {
        return precio_unitario;
    }

    public void setPrecioUnitario(BigDecimal precio_unitario) {
        this.precio_unitario = precio_unitario;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
