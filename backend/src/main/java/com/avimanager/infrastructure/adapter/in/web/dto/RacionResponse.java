package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.port.in.RacionDiaria;

/**
 * Fila de la pagina "Racion diaria" (F-06.2).
 */
public class RacionResponse {

    private final String galponId;
    private final String galponNombre;
    private final String loteId;
    private final int edadSemanas;
    private final int avesVivas;
    private final double gramosPorAve;
    private final double kilosTotales;
    private final String estrategia;

    private RacionResponse(RacionDiaria racion) {
        this.galponId = racion.getGalponId();
        this.galponNombre = racion.getGalponNombre();
        this.loteId = racion.getLoteId();
        this.edadSemanas = racion.getEdadSemanas();
        this.avesVivas = racion.getAvesVivas();
        this.gramosPorAve = racion.getGramosPorAve();
        this.kilosTotales = racion.getKilosTotales();
        this.estrategia = racion.getEstrategia();
    }

    public static RacionResponse desde(RacionDiaria racion) {
        return new RacionResponse(racion);
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

    public int getEdadSemanas() {
        return edadSemanas;
    }

    public int getAvesVivas() {
        return avesVivas;
    }

    public double getGramosPorAve() {
        return gramosPorAve;
    }

    public double getKilosTotales() {
        return kilosTotales;
    }

    public String getEstrategia() {
        return estrategia;
    }
}
