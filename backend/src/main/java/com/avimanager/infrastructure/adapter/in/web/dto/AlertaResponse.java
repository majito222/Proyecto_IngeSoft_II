package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.ResumenAlerta;

import java.time.LocalDateTime;

/**
 * Alerta sanitaria para la pagina "Alertas sanitarias" (F-02.6).
 */
public class AlertaResponse {

    private final String id;
    private final String loteId;
    private final String galponNombre;
    private final LocalDateTime fechaHora;
    private final double porcentajeMortalidad;
    private final String estado;
    private final String diagnostico;
    private final String tratamiento;
    private final String atendidaPor;
    private final String atendidaPorNombre;
    private final LocalDateTime fechaAtencion;
    private final String turnoId;
    private final String estadoTurno;
    private final String responsableTurnoNombre;

    private AlertaResponse(ResumenAlerta resumen) {
        AlertaSanitaria alerta = resumen.getAlerta();
        Turno turno = resumen.getTurno();
        this.id = alerta.getId();
        this.loteId = alerta.getLoteId();
        this.galponNombre = resumen.getGalponNombre();
        this.fechaHora = alerta.getFechaHora();
        this.porcentajeMortalidad = alerta.getPorcentajeMortalidad();
        this.estado = alerta.getEstado().name();
        this.diagnostico = alerta.getDiagnostico();
        this.tratamiento = alerta.getTratamiento();
        this.atendidaPor = alerta.getAtendidaPor();
        this.atendidaPorNombre = resumen.getAtendidaPorNombre();
        this.fechaAtencion = alerta.getFechaAtencion();
        this.turnoId = turno == null ? null : turno.getId();
        this.estadoTurno = turno == null ? null : turno.getEstado().name();
        this.responsableTurnoNombre = resumen.getResponsableTurnoNombre();
    }

    public static AlertaResponse desde(ResumenAlerta resumen) {
        return new AlertaResponse(resumen);
    }

    public String getId() {
        return id;
    }

    public String getLoteId() {
        return loteId;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public double getPorcentajeMortalidad() {
        return porcentajeMortalidad;
    }

    public String getEstado() {
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

    public String getAtendidaPorNombre() {
        return atendidaPorNombre;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public String getTurnoId() {
        return turnoId;
    }

    public String getEstadoTurno() {
        return estadoTurno;
    }

    public String getResponsableTurnoNombre() {
        return responsableTurnoNombre;
    }
}
