package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;          // ← para fechaDate

public class InventarioEmpaquetado implements Serializable {

    private Integer idInventario;
    private int     idEmpaque;
    private Integer idDistribucion;   // puede ser null
    private Integer idRepartidor;     // ← NUEVO
    private int     cantidad;         // positiva entrada | negativa salida
    private LocalDateTime fecha;
    private String  motivo;
    private int     cantidadActual;   // stock tras el movimiento
     private String nombreEmpaque;      // para reportes
    private Date   fechaDate;          // si tu DAO usa java.util.Date

    /* ---- getters / setters ---- */
    public Integer getIdInventario()          { return idInventario; }
    public void    setIdInventario(Integer v) { idInventario = v; }

    public int  getIdEmpaque()                { return idEmpaque; }
    public void setIdEmpaque(int v)           { idEmpaque = v; }

    public Integer getIdDistribucion()        { return idDistribucion; }
    public void    setIdDistribucion(Integer v){ idDistribucion = v; }

    public Integer getIdRepartidor()          { return idRepartidor; }  // ← getter
    public void    setIdRepartidor(Integer v) { idRepartidor = v; }      // ← setter

    public int  getCantidad()                 { return cantidad; }
    public void setCantidad(int v)            { cantidad = v; }

    public LocalDateTime getFecha()           { return fecha; }
    public void         setFecha(LocalDateTime v){ fecha = v; }

    public String getMotivo()                 { return motivo; }
    public void   setMotivo(String v)         { motivo = v; }

    public int  getCantidadActual()           { return cantidadActual; }
    public void setCantidadActual(int v)      { cantidadActual = v; }
    public String getNombreEmpaque()             { return nombreEmpaque; }
    public void   setNombreEmpaque(String n)     { this.nombreEmpaque = n; }

    public java.util.Date getFechaDate()        { return fechaDate; }
public void           setFechaDate(java.util.Date d){ fechaDate = d; }
}
