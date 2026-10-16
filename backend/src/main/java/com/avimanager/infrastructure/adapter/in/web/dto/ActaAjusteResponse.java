package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.ActaAjuste;

import java.time.LocalDateTime;

/** F-05: un Acta de Ajuste del historial de auditoria. */
public class ActaAjusteResponse {

    private final String id;
    private final String insumoId;
    private final String insumoNombre;
    private final String unidad;
    private final double stockSistema;
    private final double stockContado;
    private final double diferencia;
    private final String justificacion;
    private final String administradorUsername;
    private final String administradorNombre;
    private final LocalDateTime fecha;

    private ActaAjusteResponse(ActaAjuste acta) {
        this.id = acta.getId();
        this.insumoId = acta.getInsumoId();
        this.insumoNombre = acta.getInsumoNombre();
        this.unidad = acta.getUnidad();
        this.stockSistema = redondear(acta.getStockSistema());
        this.stockContado = redondear(acta.getStockContado());
        this.diferencia = redondear(acta.getDiferencia());
        this.justificacion = acta.getJustificacion();
        this.administradorUsername = acta.getAdministradorUsername();
        this.administradorNombre = acta.getAdministradorNombre();
        this.fecha = acta.getFecha();
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10) / 10.0;
    }

    public static ActaAjusteResponse desde(ActaAjuste acta) {
        return new ActaAjusteResponse(acta);
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

    public double getDiferencia() {
        return diferencia;
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
