package modelo;

public class Repartidor {
    private int idRepartidor;
    private String nombreRepartidor;
    private String apellidoRepartidor;
    private String telefono;

    public Repartidor() {
    }

    public Repartidor(int idRepartidor, String nombreRepartidor, String apellidoRepartidor, String telefono) {
        this.idRepartidor = idRepartidor;
        this.nombreRepartidor = nombreRepartidor;
        this.apellidoRepartidor = apellidoRepartidor;
        this.telefono = telefono;
    }

    public int getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(int idRepartidor) {
        this.idRepartidor = idRepartidor;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
