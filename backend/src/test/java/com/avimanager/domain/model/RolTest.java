package com.avimanager.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class RolTest {

    @Test
    void soloOperarioYAdministradorPuedenCerrarTurno() {
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.CERRAR_TURNO)))
                .containsExactlyInAnyOrder(Rol.OPERARIO, Rol.ADMINISTRADOR);
    }

    @Test
    void soloElAdministradorPuedeAjustarInventario_F05() {
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.AJUSTAR_INVENTARIO)))
                .containsExactly(Rol.ADMINISTRADOR);
    }

    @Test
    void elVeterinarioPuedeVerAlertasSanitarias() {
        assertThat(Rol.VETERINARIO.tienePermiso(Permiso.VER_ALERTAS_SANITARIAS)).isTrue();
    }

    @Test
    void soloOperarioYAdministradorPuedenIniciarTurno_F10() {
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.INICIAR_TURNO)))
                .containsExactlyInAnyOrder(Rol.OPERARIO, Rol.ADMINISTRADOR);
    }

    @Test
    void soloElAdministradorVeElDashboardDeTurnos_F10() {
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.VER_TURNOS)))
                .containsExactly(Rol.ADMINISTRADOR);
    }

    /** Matriz completa de permisos por rol, acordada para la Entrega 2. */
    @Test
    void cadaRolTieneExactamenteSusPermisos() {
        assertThat(Rol.OPERARIO.getPermisos()).containsExactlyInAnyOrder(
                Permiso.INICIAR_TURNO, Permiso.CERRAR_TURNO, Permiso.VER_LOTES,
                Permiso.VER_RACION, Permiso.REGISTRAR_PESO, Permiso.REPORTAR_DANOS);
        assertThat(Rol.ADMINISTRADOR.getPermisos()).containsExactlyInAnyOrder(Permiso.values());
        assertThat(Rol.VETERINARIO.getPermisos()).containsExactlyInAnyOrder(
                Permiso.VER_LOTES, Permiso.VER_ALERTAS_SANITARIAS, Permiso.ATENDER_ALERTAS_SANITARIAS);
        assertThat(Rol.DUENO.getPermisos()).containsExactlyInAnyOrder(
                Permiso.VER_LOTES, Permiso.VER_ALERTAS_SANITARIAS, Permiso.VER_INVENTARIO, Permiso.VER_DASHBOARD_KPI);
        assertThat(Rol.ZOOTECNISTA.getPermisos()).containsExactlyInAnyOrder(Permiso.VER_LOTES, Permiso.VER_RACION);
        assertThat(Rol.TECNICO.getPermisos()).containsExactlyInAnyOrder(
                Permiso.VER_LOTES, Permiso.ATENDER_ORDENES_MANTENIMIENTO);
    }
}
