package com.avimanager.domain.model;

/**
 * Una alerta sanitaria nace PENDIENTE (RN-01) y pasa a ATENDIDA cuando el
 * veterinario registra su diagnostico y tratamiento (F-02.5).
 */
public enum EstadoAlerta {
    PENDIENTE,
    ATENDIDA
}
