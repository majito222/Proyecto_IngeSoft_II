package com.avimanager.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio generada por RN-01 / RN-07 cuando la mortalidad diaria
 * de un lote supera el umbral critico.
 */
public class AlertaSanitaria {

    private final String id;
    private final String loteId;
    private final String reporteDiarioId;
    private final LocalDateTime fechaHora;
    private final double porcentajeMortalidad;

    public AlertaSanitaria(String id, String loteId, String reporteDiarioId,
                            LocalDateTime fechaHora, double porcentajeMortalidad) {
        this.id = id;
        this.loteId = loteId;
        this.reporteDiarioId = reporteDiarioId;
        this.fechaHora = fechaHora;
        this.porcentajeMortalidad = porcentajeMortalidad;
    }

    public String getId() {
        return id;
    }

    public String getLoteId() {
        return loteId;
    }

    public String getReporteDiarioId() {
        return reporteDiarioId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public double getPorcentajeMortalidad() {
        return porcentajeMortalidad;
    }
}
