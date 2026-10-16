package com.avimanager.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tipos de usuario del galpon (F-09) y los permisos que tiene cada uno.
 *
 * Segregacion de funciones (Student Book, actores de sistema): cada rol ejecuta
 * su parte del proceso y el Administrador supervisa y aprueba, pero no ejecuta
 * el trabajo de los demas. Asi quien aprueba un reporte no es quien lo registro.
 *   - Operario: inicia y cierra su turno, pesa y reporta danos (RN-05).
 *   - Veterinario: atiende las alertas sanitarias (RN-01).
 *   - Zootecnista: aprueba el ajuste de racion por peso (RN-02).
 *   - Tecnico: inicia y certifica las reparaciones (RN-04).
 *   - Administrador: supervisa turnos, pesajes, alertas y ordenes; aprueba
 *     reportes y consolida (F-04); es el unico que ajusta inventario (RN-08) y
 *     gestiona galpones.
 */
public enum Rol {
    OPERARIO("Operario", EnumSet.of(
            Permiso.INICIAR_TURNO, Permiso.CERRAR_TURNO, Permiso.VER_LOTES,
            Permiso.VER_RACION, Permiso.REGISTRAR_PESO, Permiso.REPORTAR_DANOS)),
    ADMINISTRADOR("Administrador", EnumSet.of(
            Permiso.VER_TURNOS, Permiso.APROBAR_REPORTES, Permiso.VER_LOTES, Permiso.GESTIONAR_GALPONES,
            Permiso.VER_ALERTAS_SANITARIAS, Permiso.VER_INVENTARIO, Permiso.AJUSTAR_INVENTARIO,
            Permiso.VER_DASHBOARD_KPI, Permiso.VER_RACION, Permiso.VER_PESAJES,
            Permiso.VER_ORDENES_MANTENIMIENTO)),
    VETERINARIO("Veterinario", EnumSet.of(
            Permiso.VER_LOTES, Permiso.VER_ALERTAS_SANITARIAS, Permiso.ATENDER_ALERTAS_SANITARIAS)),
    DUENO("Dueño", EnumSet.of(
            Permiso.VER_LOTES, Permiso.VER_ALERTAS_SANITARIAS, Permiso.VER_INVENTARIO, Permiso.VER_DASHBOARD_KPI)),
    ZOOTECNISTA("Zootecnista", EnumSet.of(
            Permiso.VER_LOTES, Permiso.VER_RACION, Permiso.VER_PESAJES, Permiso.REVISAR_PESAJES)),
    TECNICO("Técnico de mantenimiento", EnumSet.of(
            Permiso.VER_LOTES, Permiso.VER_ORDENES_MANTENIMIENTO, Permiso.ATENDER_ORDENES_MANTENIMIENTO));

    private final String descripcion;
    private final Set<Permiso> permisos;

    Rol(String descripcion, Set<Permiso> permisos) {
        this.descripcion = descripcion;
        this.permisos = permisos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Set<Permiso> getPermisos() {
        return EnumSet.copyOf(permisos);
    }

    public boolean tienePermiso(Permiso permiso) {
        return permisos.contains(permiso);
    }
}
