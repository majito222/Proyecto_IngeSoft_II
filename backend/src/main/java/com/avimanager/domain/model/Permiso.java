package com.avimanager.domain.model;

/**
 * Acciones del sistema que se autorizan por rol (F-09). Los endpoints y el
 * menu del frontend se protegen por permiso, no por rol, para que agregar o
 * cambiar un rol no obligue a tocar cada controlador.
 */
public enum Permiso {
    CERRAR_TURNO,
    VER_LOTES,
    VER_ALERTAS_SANITARIAS,
    AJUSTAR_INVENTARIO
}
