package com.avimanager.domain.port.in;

/**
 * Racion de alimento sugerida para un lote hoy (F-06).
 */
public class RacionDiaria {

    private final String galponId;
    private final String galponNombre;
    private final String loteId;
    private final int edadSemanas;
    private final int avesVivas;
    private final double gramosPorAve;
    private final double kilosTotales;
    private final String estrategia;

    public RacionDiaria(String galponId, String galponNombre, String loteId, int edadSemanas, int avesVivas,
                        double gramosPorAve, double kilosTotales, String estrategia) {
        this.galponId = galponId;
        this.galponNombre = galponNombre;
        this.loteId = loteId;
        this.edadSemanas = edadSemanas;
        this.avesVivas = avesVivas;
        this.gramosPorAve = gramosPorAve;
        this.kilosTotales = kilosTotales;
        this.estrategia = estrategia;
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
