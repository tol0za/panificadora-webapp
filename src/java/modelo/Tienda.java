package modelo;

public class Tienda {
    private int idTienda;
    private String nombre;
    private String direccion;
    private String telefono;
private String zona;

    public Tienda() {}

    public Tienda(int idTienda, String nombre, String direccion, String telefono) {
        this.idTienda = idTienda;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
    }
public String getNombreTienda() {
    return nombre;
}

public void setNombreTienda(String nombre) {
    this.nombre = nombre;
}
    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getZona() {
    return zona;
}

public void setZona(String zona) {
    this.zona = zona;
}
}
