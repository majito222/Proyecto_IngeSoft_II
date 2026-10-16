package com.avimanager.domain.model;

import com.avimanager.domain.exception.LoteYaFinalizadoException;

/**
 * Entidad de dominio: Lote de aves dentro de un galpon.
 * poblacionInicial es la poblacion con la que ingreso el lote (base de la
 * mortalidad acumulada, F-04). poblacionActual son las aves vivas: como hay un
 * solo turno por lote al dia y las bajas se descuentan al consolidar el cierre,
 * al momento del cierre es tambien la poblacion inicial del dia (RN-07).
 * Los lotes nuevos se crean con LoteBuilder (F-11), que valida la capacidad del
 * galpon y que este no tenga ya un lote activo.
 */
public class Lote {

    private final String id;
    private final String galponId;
    private final int poblacionInicial;
    private int poblacionActual;
    private final int edadSemanas;
    private EstadoLote estado;

    public Lote(String id, String galponId, int poblacionInicial, int poblacionActual, int edadSemanas, EstadoLote estado) {
        this.id = id;
        this.galponId = galponId;
        this.poblacionInicial = poblacionInicial;
        this.poblacionActual = poblacionActual;
        this.edadSemanas = edadSemanas;
        this.estado = estado;
    }

    public static LoteBuilder builder() {
        return new LoteBuilder();
    }

    public void registrarBajas(int cantidadBajas) {
        this.poblacionActual = this.poblacionActual - cantidadBajas;
    }

    public void ponerEnObservacion() {
        this.estado = EstadoLote.EN_OBSERVACION;
    }

    /** El veterinario decide sacar el lote de observacion al atender la alerta (F-02.5). */
    public void liberarDeObservacion() {
        this.estado = EstadoLote.ACTIVO;
    }

    /** F-11: el lote sale del galpon, que queda libre para recibir un lote nuevo. */
    public void finalizar() {
        if (!estaActivo()) {
            throw new LoteYaFinalizadoException(id);
        }
        this.estado = EstadoLote.FINALIZADO;
    }

    /** Un lote esta activo mientras siga alojado en su galpon (aunque este en observacion). */
    public boolean estaActivo() {
        return estado != EstadoLote.FINALIZADO;
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

    public EstadoLote getEstado() {
        return estado;
    }
}
