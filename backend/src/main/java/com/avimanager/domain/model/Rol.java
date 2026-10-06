package com.avimanager.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tipos de usuario del galpon (F-09) y los permisos que tiene cada uno.
 * Regla de F-05: solo el Administrador puede ajustar el inventario.
 * Regla de F-10: solo el Administrador ve el dashboard con todos los turnos.
 */
public enum Rol {
    OPERARIO("Operario", EnumSet.of(
            Permiso.INICIAR_TURNO, Permiso.CERRAR_TURNO, Permiso.VER_LOTES,
            Permiso.VER_RACION, Permiso.REGISTRAR_PESO, Permiso.REPORTAR_DANOS)),
    ADMINISTRADOR("Administrador", EnumSet.allOf(Permiso.class)),
    VETERINARIO("Veterinario", EnumSet.of(
            Permiso.VER_LOTES, Permiso.VER_ALERTAS_SANITARIAS, Permiso.ATENDER_ALERTAS_SANITARIAS)),
    DUENO("Dueño", EnumSet.of(
            Permiso.VER_LOTES, Permiso.VER_ALERTAS_SANITARIAS, Permiso.VER_INVENTARIO, Permiso.VER_DASHBOARD_KPI)),
    ZOOTECNISTA("Zootecnista", EnumSet.of(Permiso.VER_LOTES, Permiso.VER_RACION)),
    TECNICO("Técnico de mantenimiento", EnumSet.of(Permiso.VER_LOTES, Permiso.ATENDER_ORDENES_MANTENIMIENTO));

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
