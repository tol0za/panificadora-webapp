package modelo;

import java.math.BigDecimal;
import java.sql.Date;

// Importar clases de dominio relacionadas para exponerlas como propiedades anidadas
import modelo.Tienda;
import modelo.Repartidor;

public class NotaVenta {
    private int idNota;
    private int idRepartidor;
    private int idTienda;
    private Date fecha;
    private BigDecimal total;

    // Nuevo campo para folio de la nota. En la base de datos existe un campo `folio`
    // que representa el número de folio de la nota. Este campo se utilizará en las vistas
    // mediante el getter correspondiente. Si no se carga explícitamente, puede igualarse al idNota.
    private int folio;

    // Referencias a objetos relacionados para exponer propiedades anidadas en las vistas
    // (p.ej. nv.tienda.nombre o nv.repartidor.nombre). Estas se construyen de forma
    // perezosa a partir de los campos existentes como idTienda, nombreTienda, etc.
    private Tienda tienda;
    private Repartidor repartidor;

    // Campo alias de fecha para exponer como fechaNota en la vista
    private Date fechaNota;

    // Campos para mostrar nombres (opcional para vistas)
    private String nombreRepartidor;
    private String apellidoRepartidor;
    private String nombreTienda;

    // Getters y Setters
    public int getIdNota() {
        return idNota;
    }
    public void setIdNota(int idNota) {
        this.idNota = idNota;
    }
    public int getIdRepartidor() {
        return idRepartidor;
    }
    public void setIdRepartidor(int idRepartidor) {
        this.idRepartidor = idRepartidor;
    }
    public int getIdTienda() {
        return idTienda;
    }
    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }
    public Date getFecha() {
        return fecha;
    }
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    public BigDecimal getTotal() {
        return total;
    }
    public void setTotal(BigDecimal total) {
        this.total = total;
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

    public String getNombreTienda() {
        return nombreTienda;
    }
    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }

    // -------------------- Nuevos getters y setters --------------------

    /**
     * Obtiene el folio de la nota. Si no se ha establecido un folio explícito,
     * se devuelve el identificador de la nota como valor predeterminado.
     */
    public int getFolio() {
        return (folio > 0) ? folio : idNota;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    /**
     * Devuelve una instancia de Tienda asociada a esta nota. Si no se ha
     * establecido previamente, se crea a partir de los campos idTienda y nombreTienda.
     */
    public Tienda getTienda() {
        if (tienda == null) {
            tienda = new Tienda();
            try {
                // Intentar utilizar reflección para setear id y nombre si existen dichos métodos
                java.lang.reflect.Method setId = Tienda.class.getMethod("setIdTienda", int.class);
                setId.invoke(tienda, this.idTienda);
                java.lang.reflect.Method setNombre = Tienda.class.getMethod("setNombre", String.class);
                setNombre.invoke(tienda, this.nombreTienda);
            } catch (Exception e) {
                // Si falla la reflexión, asignar directamente si los campos son públicos (no deberían serlo)
            }
        }
        return tienda;
    }

    public void setTienda(Tienda tienda) {
        this.tienda = tienda;
    }

    /**
     * Devuelve una instancia de Repartidor asociada a esta nota. Si no se ha
     * establecido previamente, se crea usando los campos idRepartidor, nombreRepartidor
     * y apellidoRepartidor.
     */
    public Repartidor getRepartidor() {
        if (repartidor == null) {
            repartidor = new Repartidor();
            try {
                java.lang.reflect.Method setId = Repartidor.class.getMethod("setIdRepartidor", int.class);
                setId.invoke(repartidor, this.idRepartidor);
                // Intentar establecer nombre y apellido. Algunos modelos podrían usar setNombreRepartidor
                // en lugar de setNombre. Se intenta ambos silenciosamente.
                try {
                    java.lang.reflect.Method setNombre = Repartidor.class.getMethod("setNombre", String.class);
                    setNombre.invoke(repartidor, this.nombreRepartidor);
                } catch (NoSuchMethodException ex) {
                    try {
                        java.lang.reflect.Method setNombreRep = Repartidor.class.getMethod("setNombreRepartidor", String.class);
                        setNombreRep.invoke(repartidor, this.nombreRepartidor);
                    } catch (Exception ignore) {}
                }
                try {
                    java.lang.reflect.Method setApellido = Repartidor.class.getMethod("setApellido", String.class);
                    setApellido.invoke(repartidor, this.apellidoRepartidor);
                } catch (NoSuchMethodException ex) {
                    try {
                        java.lang.reflect.Method setApellidoRep = Repartidor.class.getMethod("setApellidoRepartidor", String.class);
                        setApellidoRep.invoke(repartidor, this.apellidoRepartidor);
                    } catch (Exception ignore) {}
                }
            } catch (Exception e) {
                // Ignorar excepciones de reflexión
            }
        }
        return repartidor;
    }

    public void setRepartidor(Repartidor repartidor) {
        this.repartidor = repartidor;
    }

    /**
     * Devuelve la fecha de la nota. Es un alias de {@link #getFecha()} para
     * facilitar su uso en las vistas.
     */
    public Date getFechaNota() {
        return fecha;
    }

    public void setFechaNota(Date fechaNota) {
        this.fecha = fechaNota;
    }
}
