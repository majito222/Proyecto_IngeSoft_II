package com.avimanager.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class RolTest {

    @Test
    void soloElOperarioIniciaYCierraTurno_F10() {
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.INICIAR_TURNO)))
                .containsExactly(Rol.OPERARIO);
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.CERRAR_TURNO)))
                .containsExactly(Rol.OPERARIO);
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
    void soloElAdministradorVeElDashboardDeTurnos_F10() {
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.VER_TURNOS)))
                .containsExactly(Rol.ADMINISTRADOR);
    }

    /** Cada accion la ejecuta un solo rol, el que la tiene a cargo en el Student Book. */
    @Test
    void cadaAccionDelProcesoLaEjecutaSoloSuResponsable() {
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.ATENDER_ALERTAS_SANITARIAS)))
                .as("RN-01: atiende el Veterinario").containsExactly(Rol.VETERINARIO);
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.REVISAR_PESAJES)))
                .as("RN-02: aprueba el Zootecnista").containsExactly(Rol.ZOOTECNISTA);
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.ATENDER_ORDENES_MANTENIMIENTO)))
                .as("RN-04: certifica el Tecnico").containsExactly(Rol.TECNICO);
        assertThat(Arrays.stream(Rol.values()).filter(r -> r.tienePermiso(Permiso.REPORTAR_DANOS)))
                .as("el dano lo reporta el worker").containsExactly(Rol.OPERARIO);
    }

    /** Segregacion de funciones: el Administrador supervisa y aprueba, pero no ejecuta el trabajo de otros roles. */
    @Test
    void elAdministradorSupervisaPeroNoEjecutaElTrabajoDeOtrosRoles() {
        assertThat(Rol.ADMINISTRADOR.getPermisos())
                .contains(Permiso.VER_PESAJES, Permiso.VER_ORDENES_MANTENIMIENTO, Permiso.VER_ALERTAS_SANITARIAS,
                        Permiso.APROBAR_REPORTES)
                .doesNotContain(Permiso.CERRAR_TURNO, Permiso.REGISTRAR_PESO, Permiso.REVISAR_PESAJES,
                        Permiso.ATENDER_ALERTAS_SANITARIAS, Permiso.REPORTAR_DANOS,
                        Permiso.ATENDER_ORDENES_MANTENIMIENTO);
    }

    /** Matriz completa de permisos por rol. */
    @Test
    void cadaRolTieneExactamenteSusPermisos() {
        assertThat(Rol.OPERARIO.getPermisos()).containsExactlyInAnyOrder(
                Permiso.INICIAR_TURNO, Permiso.CERRAR_TURNO, Permiso.VER_LOTES,
                Permiso.VER_RACION, Permiso.REGISTRAR_PESO, Permiso.REPORTAR_DANOS);
        assertThat(Rol.ADMINISTRADOR.getPermisos()).containsExactlyInAnyOrder(
                Permiso.VER_TURNOS, Permiso.APROBAR_REPORTES, Permiso.VER_LOTES, Permiso.GESTIONAR_GALPONES,
                Permiso.VER_ALERTAS_SANITARIAS, Permiso.VER_INVENTARIO, Permiso.AJUSTAR_INVENTARIO,
                Permiso.VER_DASHBOARD_KPI, Permiso.VER_RACION, Permiso.VER_PESAJES,
                Permiso.VER_ORDENES_MANTENIMIENTO);
        assertThat(Rol.VETERINARIO.getPermisos()).containsExactlyInAnyOrder(
                Permiso.VER_LOTES, Permiso.VER_ALERTAS_SANITARIAS, Permiso.ATENDER_ALERTAS_SANITARIAS);
        assertThat(Rol.DUENO.getPermisos()).containsExactlyInAnyOrder(
                Permiso.VER_LOTES, Permiso.VER_ALERTAS_SANITARIAS, Permiso.VER_INVENTARIO, Permiso.VER_DASHBOARD_KPI);
        assertThat(Rol.ZOOTECNISTA.getPermisos()).containsExactlyInAnyOrder(
                Permiso.VER_LOTES, Permiso.VER_RACION, Permiso.VER_PESAJES, Permiso.REVISAR_PESAJES);
        assertThat(Rol.TECNICO.getPermisos()).containsExactlyInAnyOrder(
                Permiso.VER_LOTES, Permiso.VER_ORDENES_MANTENIMIENTO, Permiso.ATENDER_ORDENES_MANTENIMIENTO);
    }
}
