package com.avimanager.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio (F-05 / RN-08): registro de auditoria de un ajuste manual
 * de inventario. Guarda el stock que tenia el sistema, el stock contado en la
 * bodega, la diferencia, la justificacion, quien lo hizo y cuando. Es inmutable:
 * un acta registrada no se edita ni se borra, para que no se oculten perdidas.
 */
public class ActaAjuste {

    private final String id;
    private final String insumoId;
    private final String insumoNombre;
    private final String unidad;
    private final double stockSistema;
    private final double stockContado;
    private final String justificacion;
    private final String administradorUsername;
    private final String administradorNombre;
    private final LocalDateTime fecha;

    public ActaAjuste(String id, Insumo insumo, double stockSistema, double stockContado, String justificacion,
                      String administradorUsername, String administradorNombre, LocalDateTime fecha) {
        this.id = id;
        this.insumoId = insumo.getId();
        this.insumoNombre = insumo.getNombre();
        this.unidad = insumo.getUnidad();
        this.stockSistema = stockSistema;
        this.stockContado = stockContado;
        this.justificacion = justificacion;
        this.administradorUsername = administradorUsername;
        this.administradorNombre = administradorNombre;
        this.fecha = fecha;
    }

    /** Positiva si en la bodega habia mas de lo que decia el sistema; negativa si faltaba. */
    public double getDiferencia() {
        return stockContado - stockSistema;
    }

    public String getId() {
        return id;
    }

    public String getInsumoId() {
        return insumoId;
    }

    public String getInsumoNombre() {
        return insumoNombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public double getStockSistema() {
        return stockSistema;
    }

    public double getStockContado() {
        return stockContado;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public String getAdministradorUsername() {
        return administradorUsername;
    }

    public String getAdministradorNombre() {
        return administradorNombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
