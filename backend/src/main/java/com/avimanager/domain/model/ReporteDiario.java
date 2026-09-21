package com.avimanager.domain.model;

import java.time.LocalDate;

/**
 * Entidad de dominio: reporte de turno de un worker para un lote, en una fecha dada.
 * Consolida los datos obligatorios de consumo y mortalidad (RN-05) y el resultado
 * del calculo de mortalidad (RN-07).
 */
public class ReporteDiario {

    private final String id;
    private final String loteId;
    private final LocalDate fecha;
    private final Double consumoAlimentoKg;
    private final Integer cantidadBajas;
    private final String causaProbableMortalidad;
    private final Integer produccionHuevosBandejas;
    private final String novedades;
    private EstadoReporte estado;
    private Double porcentajeMortalidad;

    public ReporteDiario(String id, String loteId, LocalDate fecha, Double consumoAlimentoKg,
                          Integer cantidadBajas, String causaProbableMortalidad,
                          Integer produccionHuevosBandejas, String novedades) {
        this.id = id;
        this.loteId = loteId;
        this.fecha = fecha;
        this.consumoAlimentoKg = consumoAlimentoKg;
        this.cantidadBajas = cantidadBajas;
        this.causaProbableMortalidad = causaProbableMortalidad;
        this.produccionHuevosBandejas = produccionHuevosBandejas;
        this.novedades = novedades;
        this.estado = EstadoReporte.EN_PROCESO;
    }

    public void consolidar() {
        this.estado = EstadoReporte.CONSOLIDADO;
    }

    public void bloquearPorAlertaSanitaria(double porcentajeMortalidad) {
        this.porcentajeMortalidad = porcentajeMortalidad;
        this.estado = EstadoReporte.BLOQUEADO_ALERTA_SANITARIA;
    }

    public void registrarPorcentajeMortalidad(double porcentajeMortalidad) {
        this.porcentajeMortalidad = porcentajeMortalidad;
    }

    public String getId() {
        return id;
    }

    public String getLoteId() {
        return loteId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Double getConsumoAlimentoKg() {
        return consumoAlimentoKg;
    }

    public Integer getCantidadBajas() {
        return cantidadBajas;
    }

    public String getCausaProbableMortalidad() {
        return causaProbableMortalidad;
    }

    public Integer getProduccionHuevosBandejas() {
        return produccionHuevosBandejas;
    }

    public String getNovedades() {
        return novedades;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    public Double getPorcentajeMortalidad() {
        return porcentajeMortalidad;
    }
}
