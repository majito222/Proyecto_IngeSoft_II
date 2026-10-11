package com.avimanager.domain.port.in;

/**
 * Lo que el worker ve antes de pesar (F-07.1): su galpon, el lote, el peso
 * estandar de la semana y, si ya peso esta semana, ese pesaje.
 */
public class ReferenciaPesaje {

    private final String galponId;
    private final String galponNombre;
    private final String loteId;
    private final int edadSemanas;
    private final int avesVivas;
    private final double pesoEstandarGramos;
    private final ResumenPesaje pesajeDeLaSemana;

    public ReferenciaPesaje(String galponId, String galponNombre, String loteId, int edadSemanas, int avesVivas,
                            double pesoEstandarGramos, ResumenPesaje pesajeDeLaSemana) {
        this.galponId = galponId;
        this.galponNombre = galponNombre;
        this.loteId = loteId;
        this.edadSemanas = edadSemanas;
        this.avesVivas = avesVivas;
        this.pesoEstandarGramos = pesoEstandarGramos;
        this.pesajeDeLaSemana = pesajeDeLaSemana;
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

    public double getPesoEstandarGramos() {
        return pesoEstandarGramos;
    }

    /** null si el lote todavia no se ha pesado esta semana. */
    public ResumenPesaje getPesajeDeLaSemana() {
        return pesajeDeLaSemana;
    }
}
