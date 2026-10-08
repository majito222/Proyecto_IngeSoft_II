package com.avimanager.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio (F-10): la jornada de un worker sobre el lote de su galpon
 * en una fecha. Un lote tiene un solo turno por dia, porque RN-07 calcula la
 * mortalidad con las bajas del dia completo.
 * Las transiciones de estado las decide EstadoTurno (patron State): Turno solo
 * guarda los datos de cada transicion y delega en su estado actual.
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
        this.estado = estado.cerrar();
        this.reporteDiarioId = reporte.getId();
        this.porcentajeMortalidad = reporte.getPorcentajeMortalidad();
        this.horaCierre = ahora;
    }

    public void bloquearPorAlertaSanitaria(ReporteDiario reporte, AlertaSanitaria alerta) {
        this.estado = estado.bloquearPorAlertaSanitaria();
        this.reporteDiarioId = reporte.getId();
        this.porcentajeMortalidad = reporte.getPorcentajeMortalidad();
        this.alertaSanitariaId = alerta.getId();
    }

    /**
     * F-02.5: el veterinario atendio la alerta, asi que el turno bloqueado se
     * cierra con alerta. El reporte sigue siendo del worker responsable.
     */
    public void cerrarConAlertaAtendida(LocalDateTime ahora) {
        this.estado = estado.cerrarConAlertaAtendida();
        this.horaCierre = ahora;
    }

    public boolean esResponsable(String username) {
        return workerUsername.equals(username);
    }

    public boolean estaAbierto() {
        return estado.admiteCierre();
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
