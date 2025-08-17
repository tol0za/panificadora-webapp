package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * POJO que representa una línea de la tabla <code>distribucion</code>.
 * <p>Incluye:</p>
 * <ul>
 *   <li>Constructor vacío – requerido por JSP, frameworks y DAOs con setters.</li>
 *   <li>Constructor completo – práctico para mapeos directos.</li>
 *   <li>Getters y setters convencionales.</li>
 * </ul>
 */
public class Distribucion implements Serializable {

    /* ---------- campos ---------- */
    private int             idDistribucion;
    private int             idRepartidor;
    private int             idEmpaque;
    private String          nombreEmpaque;
    private int             cantidad;
    private LocalDateTime   fechaDistribucion;
    private String          nombreRepartidorCompleto;

    /* ---------- constructores ---------- */

    /** Constructor vacío (requerido por beans). */
    public Distribucion() {}

    /** Constructor completo – útil para proyecciones JOIN en DAO. */
    public Distribucion(int idDistribucion,
                        int idRepartidor,
                        int idEmpaque,
                        String nombreEmpaque,
                        int cantidad,
                        LocalDateTime fechaDistribucion,
                        String nombreRepartidorCompleto) {
        this.idDistribucion          = idDistribucion;
        this.idRepartidor            = idRepartidor;
        this.idEmpaque               = idEmpaque;
        this.nombreEmpaque           = nombreEmpaque;
        this.cantidad                = cantidad;
        this.fechaDistribucion       = fechaDistribucion;
        this.nombreRepartidorCompleto= nombreRepartidorCompleto;
    }

    /* ---------- getters ---------- */
    public int            getIdDistribucion()         { return idDistribucion; }
    public int            getIdRepartidor()           { return idRepartidor; }
    public int            getIdEmpaque()              { return idEmpaque; }
    public String         getNombreEmpaque()          { return nombreEmpaque; }
    public int            getCantidad()               { return cantidad; }
    public LocalDateTime  getFechaDistribucion()      { return fechaDistribucion; }
    public String         getNombreRepartidorCompleto(){ return nombreRepartidorCompleto; }

    /* ---------- setters ---------- */
    public void setIdDistribucion(int idDistribucion)              { this.idDistribucion = idDistribucion; }
    public void setIdRepartidor(int idRepartidor)                  { this.idRepartidor = idRepartidor; }
    public void setIdEmpaque(int idEmpaque)                        { this.idEmpaque = idEmpaque; }
    public void setNombreEmpaque(String nombreEmpaque)             { this.nombreEmpaque = nombreEmpaque; }
    public void setCantidad(int cantidad)                          { this.cantidad = cantidad; }
    public void setFechaDistribucion(LocalDateTime fechaDistribucion){ this.fechaDistribucion = fechaDistribucion; }
    public void setNombreRepartidorCompleto(String nombreCompleto) { this.nombreRepartidorCompleto = nombreCompleto; }
}
