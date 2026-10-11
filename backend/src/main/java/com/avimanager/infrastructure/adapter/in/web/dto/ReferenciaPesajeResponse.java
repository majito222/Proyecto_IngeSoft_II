package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.port.in.ReferenciaPesaje;

/**
 * Lo que ve el worker en "Pesaje semanal" antes de pesar (F-07.1).
 */
public class ReferenciaPesajeResponse {

    private final String galponId;
    private final String galponNombre;
    private final String loteId;
    private final int edadSemanas;
    private final int avesVivas;
    private final double pesoEstandarGramos;
    private final PesajeResponse pesajeDeLaSemana;

    private ReferenciaPesajeResponse(ReferenciaPesaje referencia) {
        this.galponId = referencia.getGalponId();
        this.galponNombre = referencia.getGalponNombre();
        this.loteId = referencia.getLoteId();
        this.edadSemanas = referencia.getEdadSemanas();
        this.avesVivas = referencia.getAvesVivas();
        this.pesoEstandarGramos = referencia.getPesoEstandarGramos();
        this.pesajeDeLaSemana = PesajeResponse.desde(referencia.getPesajeDeLaSemana());
    }

    public static ReferenciaPesajeResponse desde(ReferenciaPesaje referencia) {
        return new ReferenciaPesajeResponse(referencia);
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

    public PesajeResponse getPesajeDeLaSemana() {
        return pesajeDeLaSemana;
    }
}
