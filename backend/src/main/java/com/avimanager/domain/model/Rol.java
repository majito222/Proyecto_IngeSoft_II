package com.avimanager.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tipos de usuario del galpon (F-09) y los permisos que tiene cada uno.
 * Regla de F-05: solo el Administrador puede ajustar el inventario.
 * Regla de F-10: solo el Administrador ve el dashboard con todos los turnos.
 */
public enum Rol {
    OPERARIO("Operario", EnumSet.of(Permiso.INICIAR_TURNO, Permiso.CERRAR_TURNO, Permiso.VER_LOTES)),
    ADMINISTRADOR("Administrador", EnumSet.allOf(Permiso.class)),
    VETERINARIO("Veterinario", EnumSet.of(Permiso.VER_ALERTAS_SANITARIAS, Permiso.VER_LOTES)),
    DUENO("Dueño", EnumSet.of(Permiso.VER_ALERTAS_SANITARIAS, Permiso.VER_LOTES)),
    ZOOTECNISTA("Zootecnista", EnumSet.of(Permiso.VER_LOTES)),
    TECNICO("Técnico de mantenimiento", EnumSet.of(Permiso.VER_LOTES));

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
