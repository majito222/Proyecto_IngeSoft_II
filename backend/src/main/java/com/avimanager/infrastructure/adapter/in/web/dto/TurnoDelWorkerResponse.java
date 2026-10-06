package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.port.in.TurnoDelWorker;

/**
 * Pantalla del worker (F-10.2): su galpon asignado, el lote y el turno de hoy
 * (null si todavia no lo ha iniciado).
 */
public class TurnoDelWorkerResponse {

    private final String galponId;
    private final String galponNombre;
    private final String loteId;
    private final int poblacionActual;
    private final int edadSemanas;
    private final String estadoLote;
    private final TurnoResponse turnoDeHoy;

    private TurnoDelWorkerResponse(TurnoDelWorker turnoDelWorker) {
        this.galponId = turnoDelWorker.getGalpon().getId();
        this.galponNombre = turnoDelWorker.getGalpon().getNombre();
        this.loteId = turnoDelWorker.getLote().getId();
        this.poblacionActual = turnoDelWorker.getLote().getPoblacionActual();
        this.edadSemanas = turnoDelWorker.getLote().getEdadSemanas();
        this.estadoLote = turnoDelWorker.getLote().getEstado().name();
        this.turnoDeHoy = TurnoResponse.desde(turnoDelWorker.getTurnoDeHoy());
    }

    public static TurnoDelWorkerResponse desde(TurnoDelWorker turnoDelWorker) {
        return new TurnoDelWorkerResponse(turnoDelWorker);
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

    public int getPoblacionActual() {
        return poblacionActual;
    }

    public int getEdadSemanas() {
        return edadSemanas;
    }

    public String getEstadoLote() {
        return estadoLote;
    }

    public TurnoResponse getTurnoDeHoy() {
        return turnoDeHoy;
    }
}
