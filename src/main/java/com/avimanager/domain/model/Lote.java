package com.avimanager.domain.model;

/**
 * Entidad de dominio: Lote de aves dentro de un galpon.
 * poblacionInicialDia es la base para calcular el % de mortalidad diaria (RN-07).
 */
public class Lote {

    private final String id;
    private final String galponId;
    private final int poblacionInicialDia;
    private int poblacionActual;
    private final int edadSemanas;
    private EstadoLote estado;

    public Lote(String id, String galponId, int poblacionInicialDia, int poblacionActual, int edadSemanas, EstadoLote estado) {
        this.id = id;
        this.galponId = galponId;
        this.poblacionInicialDia = poblacionInicialDia;
        this.poblacionActual = poblacionActual;
        this.edadSemanas = edadSemanas;
        this.estado = estado;
    }

    public void registrarBajas(int cantidadBajas) {
        this.poblacionActual = this.poblacionActual - cantidadBajas;
    }

    public void ponerEnObservacion() {
        this.estado = EstadoLote.EN_OBSERVACION;
    }

    public String getId() {
        return id;
    }

    public String getGalponId() {
        return galponId;
    }

    public int getPoblacionInicialDia() {
        return poblacionInicialDia;
    }

    public int getPoblacionActual() {
        return poblacionActual;
    }

    public int getEdadSemanas() {
        return edadSemanas;
    }

    public EstadoLote getEstado() {
        return estado;
    }
}
