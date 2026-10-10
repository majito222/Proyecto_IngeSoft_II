package com.avimanager.domain.service.racion;

import com.avimanager.domain.model.Lote;

/**
 * Patron STRATEGY para el calculo de la racion diaria (F-06): cada estrategia
 * define cuantos gramos de alimento le corresponden a cada ave del lote. El
 * servicio multiplica por las aves vivas sin saber que estrategia se uso.
 *
 * Estrategias:
 *   RacionTablaEstandar     tabla de nutricion por semana de edad (F-06)
 *   (F-07) racion ajustada por peso cuando el pesaje se desvia mas de 5% (RN-02)
 */
public interface EstrategiaRacion {

    /** Gramos de alimento por ave para el dia de hoy. */
    double gramosPorAve(Lote lote);

    /** Nombre legible de la estrategia, para mostrar de donde sale la racion. */
    String nombre();
}
