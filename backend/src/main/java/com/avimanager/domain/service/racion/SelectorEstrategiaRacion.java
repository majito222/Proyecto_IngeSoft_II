package com.avimanager.domain.service.racion;

import com.avimanager.domain.model.Lote;

/**
 * Elige la estrategia de racion para cada lote (contexto del patron Strategy).
 * Hoy todos los lotes usan la tabla estandar; con F-07 se elegira la racion
 * ajustada por peso cuando el ultimo pesaje se desvie mas de 5% (RN-02).
 */
public class SelectorEstrategiaRacion {

    private final EstrategiaRacion tablaEstandar;

    public SelectorEstrategiaRacion(EstrategiaRacion tablaEstandar) {
        this.tablaEstandar = tablaEstandar;
    }

    public EstrategiaRacion para(Lote lote) {
        return tablaEstandar;
    }
}
