package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.port.in.CerrarTurnoResult;

import java.util.List;

public class CerrarTurnoResponse {

    private final String estado;
    private final boolean turnoCerrado;
    private final List<String> camposFaltantes;
    private final String mensaje;
    private final Double porcentajeMortalidad;
    private final String reporteId;
    private final String alertaId;
    private final String turnoId;
    private final String estadoTurno;

    private CerrarTurnoResponse(String estado, boolean turnoCerrado, List<String> camposFaltantes,
                                 String mensaje, Double porcentajeMortalidad, String reporteId, String alertaId,
                                 CerrarTurnoResult resultado) {
        this.estado = estado;
        this.turnoCerrado = turnoCerrado;
        this.camposFaltantes = camposFaltantes;
        this.mensaje = mensaje;
        this.porcentajeMortalidad = porcentajeMortalidad;
        this.reporteId = reporteId;
        this.alertaId = alertaId;
        this.turnoId = resultado.getTurno().getId();
        this.estadoTurno = resultado.getTurno().getEstado().name();
    }

    public static CerrarTurnoResponse desde(CerrarTurnoResult resultado) {
        return switch (resultado.getEstado()) {
            case CAMPOS_INCOMPLETOS -> new CerrarTurnoResponse(
                    resultado.getEstado().name(), false, resultado.getCamposFaltantes(),
                    "No se puede cerrar el turno: faltan campos obligatorios por diligenciar.",
                    null, null, null, resultado);
            case ALERTA_SANITARIA -> new CerrarTurnoResponse(
                    resultado.getEstado().name(), false, List.of(),
                    "Cierre bloqueado: la mortalidad supero el umbral critico. Se genero una alerta sanitaria "
                            + "y se notifico al veterinario.",
                    resultado.getReporteDiario().getPorcentajeMortalidad(),
                    resultado.getReporteDiario().getId(),
                    resultado.getAlertaSanitaria().getId(), resultado);
            case TURNO_CERRADO -> new CerrarTurnoResponse(
                    resultado.getEstado().name(), true, List.of(),
                    "Turno cerrado y reporte consolidado exitosamente.",
                    resultado.getReporteDiario().getPorcentajeMortalidad(),
                    resultado.getReporteDiario().getId(),
                    null, resultado);
        };
    }

    public String getTurnoId() {
        return turnoId;
    }

    public String getEstadoTurno() {
        return estadoTurno;
    }

    public String getEstado() {
        return estado;
    }

    public boolean isTurnoCerrado() {
        return turnoCerrado;
    }

    public List<String> getCamposFaltantes() {
        return camposFaltantes;
    }

    public String getMensaje() {
        return mensaje;
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
