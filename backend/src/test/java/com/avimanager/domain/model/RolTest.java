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
}
