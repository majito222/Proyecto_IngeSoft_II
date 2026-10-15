package com.avimanager.domain.model;

/**
 * ACTIVO y EN_OBSERVACION son lotes que siguen alojados en su galpon.
 * FINALIZADO (F-11) es un lote que ya salio del galpon: deja de contar como el
 * lote activo, y el galpon puede recibir uno nuevo.
 */
public enum EstadoLote {
    ACTIVO,
    EN_OBSERVACION,
    FINALIZADO
}
