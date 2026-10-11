package com.avimanager.domain.model;

/**
 * Estados de un pesaje semanal (F-07):
 *   DENTRO_DE_RANGO      la desviacion no supera +-5%: no hay nada que revisar
 *   PENDIENTE_REVISION   se desvio mas de +-5% (RN-02): espera al Zootecnista
 *   AJUSTE_APROBADO      el Zootecnista aprobo ajustar la racion del lote
 *   AJUSTE_DESCARTADO    el Zootecnista decidio mantener la racion estandar
 */
public enum EstadoPesaje {
    DENTRO_DE_RANGO,
    PENDIENTE_REVISION,
    AJUSTE_APROBADO,
    AJUSTE_DESCARTADO
}
