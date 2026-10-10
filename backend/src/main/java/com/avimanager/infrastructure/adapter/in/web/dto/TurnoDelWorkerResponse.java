package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.port.in.RacionDiaria;
import com.avimanager.domain.port.in.TurnoDelWorker;

/**
 * Pantalla del worker (F-10.2): su galpon asignado, el lote y el turno de hoy
 * (null si todavia no lo ha iniciado). Incluye la racion sugerida de hoy (F-06),
 * que el worker consulta antes de suministrar el alimento.
 */
public class TurnoDelWorkerResponse {

    private final String galponId;
    private final String galponNombre;
    private final String loteId;
    private final int poblacionActual;
    private final int edadSemanas;
    private final String estadoLote;
    private final TurnoResponse turnoDeHoy;
    private final Double racionSugeridaKg;

    private TurnoDelWorkerResponse(TurnoDelWorker turnoDelWorker, RacionDiaria racion) {
        this.galponId = turnoDelWorker.getGalpon().getId();
        this.galponNombre = turnoDelWorker.getGalpon().getNombre();
        this.loteId = turnoDelWorker.getLote().getId();
        this.poblacionActual = turnoDelWorker.getLote().getPoblacionActual();
        this.edadSemanas = turnoDelWorker.getLote().getEdadSemanas();
        this.estadoLote = turnoDelWorker.getLote().getEstado().name();
        this.turnoDeHoy = TurnoResponse.desde(turnoDelWorker.getTurnoDeHoy());
        this.racionSugeridaKg = racion == null ? null : racion.getKilosTotales();
    }

    public static TurnoDelWorkerResponse desde(TurnoDelWorker turnoDelWorker, RacionDiaria racion) {
        return new TurnoDelWorkerResponse(turnoDelWorker, racion);
    }

    public Double getRacionSugeridaKg() {
        return racionSugeridaKg;
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
