package com.avimanager.domain.model;

import com.avimanager.domain.exception.TurnoNoDisponibleException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio (F-10): la jornada de un worker sobre el lote de su galpon
 * en una fecha. Un lote tiene un solo turno por dia, porque RN-07 calcula la
 * mortalidad con las bajas del dia completo.
 */
public class Turno {

    private final String id;
    private final String workerUsername;
    private final String loteId;
    private final String galponId;
    private final LocalDate fecha;
    private final LocalDateTime horaInicio;
    private LocalDateTime horaCierre;
    private EstadoTurno estado;
    private String reporteDiarioId;
    private Double porcentajeMortalidad;
    private String alertaSanitariaId;

    public Turno(String id, String workerUsername, String loteId, String galponId,
                 LocalDate fecha, LocalDateTime horaInicio) {
        this.id = id;
        this.workerUsername = workerUsername;
        this.loteId = loteId;
        this.galponId = galponId;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.estado = EstadoTurno.ABIERTO;
    }

    public void cerrar(ReporteDiario reporte, LocalDateTime ahora) {
        exigirAbierto();
        this.reporteDiarioId = reporte.getId();
        this.porcentajeMortalidad = reporte.getPorcentajeMortalidad();
        this.horaCierre = ahora;
        this.estado = EstadoTurno.CERRADO;
    }

    public void bloquearPorAlertaSanitaria(ReporteDiario reporte, AlertaSanitaria alerta) {
        exigirAbierto();
        this.reporteDiarioId = reporte.getId();
        this.porcentajeMortalidad = reporte.getPorcentajeMortalidad();
        this.alertaSanitariaId = alerta.getId();
        this.estado = EstadoTurno.BLOQUEADO_ALERTA_SANITARIA;
    }

    public boolean esResponsable(String username) {
        return workerUsername.equals(username);
    }

    public boolean estaAbierto() {
        return estado == EstadoTurno.ABIERTO;
    }

    private void exigirAbierto() {
        if (!estaAbierto()) {
            throw new TurnoNoDisponibleException("El turno ya no está abierto (estado " + estado + ")");
        }
    }

    public String getId() {
        return id;
    }

    public String getWorkerUsername() {
        return workerUsername;
    }

    public String getLoteId() {
        return loteId;
    }

    public String getGalponId() {
        return galponId;
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

    public EstadoTurno getEstado() {
        return estado;
    }

    public String getReporteDiarioId() {
        return reporteDiarioId;
    }

    public Double getPorcentajeMortalidad() {
        return porcentajeMortalidad;
    }

    public String getAlertaSanitariaId() {
        return alertaSanitariaId;
    }
}
