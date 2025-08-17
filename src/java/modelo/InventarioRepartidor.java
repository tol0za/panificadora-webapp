package modelo;

import java.io.Serializable;

/**
 * POJO que refleja el estado agregado de inventario por repartidor‑empaque.
 * Solo se llena en consultas; las operaciones de +/‑ se delegan a InventarioRepartidorDAO.
 */
public class InventarioRepartidor implements Serializable {

    private int idRepartidor;
    private int idEmpaque;
    private int cantidadDistribuida;
    private int cantidadVendida;
    private int cantidadMermada;
    private int cantidadRestante;

    /* Constructor vacío requerido por frameworks de serialización/JSP  */
    public InventarioRepartidor() { }

    public InventarioRepartidor(int idRepartidor,
                                int idEmpaque,
                                int cantidadDistribuida,
                                int cantidadVendida,
                                int cantidadMermada,
                                int cantidadRestante) {
        this.idRepartidor        = idRepartidor;
        this.idEmpaque           = idEmpaque;
        this.cantidadDistribuida = cantidadDistribuida;
        this.cantidadVendida     = cantidadVendida;
        this.cantidadMermada     = cantidadMermada;
        this.cantidadRestante    = cantidadRestante;
    }

    /* ---------- getters & setters ---------- */

    public int getIdRepartidor()         { return idRepartidor; }
    public void setIdRepartidor(int id)  { this.idRepartidor = id; }

    public int getIdEmpaque()            { return idEmpaque; }
    public void setIdEmpaque(int id)     { this.idEmpaque = id; }

    public int getCantidadDistribuida()  { return cantidadDistribuida; }
    public void setCantidadDistribuida(int c) { this.cantidadDistribuida = c; }

    public int getCantidadVendida()      { return cantidadVendida; }
    public void setCantidadVendida(int c){ this.cantidadVendida = c; }

    public int getCantidadMermada()      { return cantidadMermada; }
    public void setCantidadMermada(int c){ this.cantidadMermada = c; }

    public int getCantidadRestante()     { return cantidadRestante; }
    public void setCantidadRestante(int c){ this.cantidadRestante = c; }
}
