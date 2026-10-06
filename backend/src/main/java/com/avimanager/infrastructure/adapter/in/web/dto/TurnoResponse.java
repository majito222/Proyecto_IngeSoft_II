package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.ResumenTurno;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fila del dashboard de cierres pendientes (F-10.3).
 */
public class TurnoResponse {

    private final String id;
    private final String responsableUsername;
    private final String responsableNombre;
    private final String galponId;
    private final String galponNombre;
    private final String loteId;
    private final LocalDate fecha;
    private final LocalDateTime horaInicio;
    private final LocalDateTime horaCierre;
    private final String estado;
    private final Double porcentajeMortalidad;
    private final String reporteId;
    private final String alertaId;

    private TurnoResponse(ResumenTurno resumen) {
        Turno turno = resumen.getTurno();
        this.id = turno.getId();
        this.responsableUsername = turno.getWorkerUsername();
        this.responsableNombre = resumen.getResponsableNombre();
        this.galponId = turno.getGalponId();
        this.galponNombre = resumen.getGalponNombre();
        this.loteId = turno.getLoteId();
        this.fecha = turno.getFecha();
        this.horaInicio = turno.getHoraInicio();
        this.horaCierre = turno.getHoraCierre();
        this.estado = turno.getEstado().name();
        this.porcentajeMortalidad = turno.getPorcentajeMortalidad();
        this.reporteId = turno.getReporteDiarioId();
        this.alertaId = turno.getAlertaSanitariaId();
    }

    public static TurnoResponse desde(ResumenTurno resumen) {
        return resumen == null ? null : new TurnoResponse(resumen);
    }

    public String getId() {
        return id;
    }

    public String getResponsableUsername() {
        return responsableUsername;
    }

    public String getResponsableNombre() {
        return responsableNombre;
    }

    public String getGalponId() {
        return galponId;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public String getLoteId() {
        return loteId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public LocalDateTime getHoraCierre() {
        return horaCierre;
    }

    public String getEstado() {
        return estado;
    }

    public Double getPorcentajeMortalidad() {
        return porcentajeMortalidad;
    }

    public String getReporteId() {
        return reporteId;
    }

    public String getAlertaId() {
        return alertaId;
    }
}
