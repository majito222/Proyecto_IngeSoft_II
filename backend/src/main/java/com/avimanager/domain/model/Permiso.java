package com.avimanager.domain.model;

/**
 * Acciones del sistema que se autorizan por rol (F-09). Los endpoints y el
 * menu del frontend se protegen por permiso, no por rol, para que agregar o
 * cambiar un rol no obligue a tocar cada controlador.
 *
 * Estan definidos todos los permisos de la Entrega 2, aunque algunos aun no
 * tengan endpoint, para que cada funcionalidad nueva solo tenga que usarlos.
 */
public enum Permiso {
    // F-10: turno operativo
    INICIAR_TURNO,
    CERRAR_TURNO,
    VER_TURNOS,
    // F-04.3: aprobar el reporte de cada worker
    APROBAR_REPORTES,
    // F-11: galpones, lotes y asignacion de workers
    VER_LOTES,
    GESTIONAR_GALPONES,
    // F-02: alertas sanitarias
    VER_ALERTAS_SANITARIAS,
    ATENDER_ALERTAS_SANITARIAS,
    // F-03 / F-05: inventario
    VER_INVENTARIO,
    AJUSTAR_INVENTARIO,
    // F-04: KPIs del dia
    VER_DASHBOARD_KPI,
    // F-06 / F-07: alimentacion y crecimiento
    VER_RACION,
    REGISTRAR_PESO,
    // F-08: infraestructura y mantenimiento
    REPORTAR_DANOS,
    ATENDER_ORDENES_MANTENIMIENTO
}
