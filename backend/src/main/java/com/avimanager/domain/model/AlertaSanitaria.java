package com.avimanager.domain.model;

import com.avimanager.domain.exception.AlertaYaAtendidaException;

import java.time.LocalDateTime;

/**
 * Entidad de dominio generada por RN-01 / RN-07 cuando la mortalidad diaria
 * de un lote supera el umbral critico. Nace PENDIENTE y el veterinario la
 * atiende registrando diagnostico y tratamiento (F-02.5).
 */
public class AlertaSanitaria {

    private final String id;
    private final String loteId;
    private final String reporteDiarioId;
    private final LocalDateTime fechaHora;
    private final double porcentajeMortalidad;
    private EstadoAlerta estado;
    private String diagnostico;
    private String tratamiento;
    private String atendidaPor;
    private LocalDateTime fechaAtencion;

    public AlertaSanitaria(String id, String loteId, String reporteDiarioId,
                            LocalDateTime fechaHora, double porcentajeMortalidad) {
        this.id = id;
        this.loteId = loteId;
        this.reporteDiarioId = reporteDiarioId;
        this.fechaHora = fechaHora;
        this.porcentajeMortalidad = porcentajeMortalidad;
        this.estado = EstadoAlerta.PENDIENTE;
    }

    /**
     * Registra la atencion del veterinario. Una alerta se atiende una sola vez y
     * el diagnostico y el tratamiento son obligatorios, para que la decision
     * quede trazada.
     */
    public void atender(String diagnostico, String tratamiento, String veterinarioUsername, LocalDateTime ahora) {
        if (estado == EstadoAlerta.ATENDIDA) {
            throw new AlertaYaAtendidaException(id);
        }
        if (diagnostico == null || diagnostico.isBlank()) {
            throw new IllegalArgumentException("El diagnóstico es obligatorio para atender la alerta");
        }
        if (tratamiento == null || tratamiento.isBlank()) {
            throw new IllegalArgumentException("El tratamiento es obligatorio para atender la alerta");
        }
        this.diagnostico = diagnostico.trim();
        this.tratamiento = tratamiento.trim();
        this.atendidaPor = veterinarioUsername;
        this.fechaAtencion = ahora;
        this.estado = EstadoAlerta.ATENDIDA;
    }

    public boolean estaPendiente() {
        return estado == EstadoAlerta.PENDIENTE;
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

    public EstadoAlerta getEstado() {
        return estado;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public String getAtendidaPor() {
        return atendidaPor;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }
}
