package com.avimanager.infrastructure.adapter.in.web.dto;

import com.avimanager.domain.model.Lote;

/** Lote de un galpon (F-11). */
public class LoteResponse {

    private final String id;
    private final String galponId;
    private final int poblacionInicial;
    private final int poblacionActual;
    private final int edadSemanas;
    private final String estado;

    private LoteResponse(Lote lote) {
        this.id = lote.getId();
        this.galponId = lote.getGalponId();
        this.poblacionInicial = lote.getPoblacionInicialDia();
        this.poblacionActual = lote.getPoblacionActual();
        this.edadSemanas = lote.getEdadSemanas();
        this.estado = lote.getEstado().name();
    }

    public static LoteResponse desde(Lote lote) {
        return lote == null ? null : new LoteResponse(lote);
    }

    public String getId() {
        return id;
    }

    public String getGalponId() {
        return galponId;
    }

    public int getPoblacionInicial() {
        return poblacionInicial;
    }

    public int getPoblacionActual() {
        return poblacionActual;
    }

    public int getEdadSemanas() {
        return edadSemanas;
    }

    public String getEstado() {
        return estado;
    }
}
