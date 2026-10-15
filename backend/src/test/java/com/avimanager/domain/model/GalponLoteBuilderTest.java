package com.avimanager.domain.model;

import com.avimanager.domain.exception.CapacidadGalponExcedidaException;
import com.avimanager.domain.exception.GalponConLoteActivoException;
import com.avimanager.domain.exception.LoteYaFinalizadoException;
import com.avimanager.domain.exception.UsuarioNoEsWorkerException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-11.3: el patron Builder de Galpon y Lote aplica las reglas de creacion
 * (capacidad maxima y un solo lote activo por galpon) antes de que el objeto exista.
 */
class GalponLoteBuilderTest {

    private final Galpon galpon = Galpon.builder().id("galpon-1").nombre("Galpón 1").capacidadMaxima(600).build();

    private Lote lote(String id, int poblacion, Lote loteActivoDelGalpon) {
        return Lote.builder().id(id).alojadoEn(galpon, loteActivoDelGalpon).poblacionInicial(poblacion).edadSemanas(1)
                .build();
    }

    @Test
    void elBuilderCreaUnGalponActivoConSusDatos() {
        Galpon creado = Galpon.builder().id("g").nombre("  Galpón Norte ").capacidadMaxima(800).build();

        assertThat(creado.getNombre()).isEqualTo("Galpón Norte");
        assertThat(creado.getCapacidadMaxima()).isEqualTo(800);
        assertThat(creado.getEstado()).isEqualTo(EstadoGalpon.ACTIVO);
    }

    @Test
    void unGalponSinNombreOConCapacidadInvalidaNoSeCrea() {
        assertThatThrownBy(() -> Galpon.builder().id("g").nombre(" ").capacidadMaxima(100).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Galpon.builder().id("g").nombre("Galpón").capacidadMaxima(0).build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Galpon.builder().id("g").nombre("Galpón").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unLoteIgualALaCapacidadDelGalponSeCreaActivoConSuPoblacion() {
        Lote creado = lote("lote-1", 600, null);

        assertThat(creado.getGalponId()).isEqualTo("galpon-1");
        assertThat(creado.getPoblacionInicialDia()).isEqualTo(600);
        assertThat(creado.getPoblacionActual()).isEqualTo(600);
        assertThat(creado.getEstado()).isEqualTo(EstadoLote.ACTIVO);
    }

    @Test
    void unLoteQueSuperaLaCapacidadDelGalponSeRechaza() {
        assertThatThrownBy(() -> lote("lote-1", 601, null))
                .isInstanceOf(CapacidadGalponExcedidaException.class)
                .hasMessageContaining("600");
    }

    @Test
    void unGalponConUnLoteActivoNoRecibeOtro() {
        Lote activo = lote("lote-1", 500, null);

        assertThatThrownBy(() -> lote("lote-2", 100, activo))
                .isInstanceOf(GalponConLoteActivoException.class)
                .hasMessageContaining("lote-1");
    }

    @Test
    void unLoteEnObservacionSigueContandoComoActivo() {
        Lote enObservacion = lote("lote-1", 500, null);
        enObservacion.ponerEnObservacion();

        assertThatThrownBy(() -> lote("lote-2", 100, enObservacion))
                .isInstanceOf(GalponConLoteActivoException.class);
    }

    @Test
    void alFinalizarElLoteElGalponPuedeRecibirUnoNuevo() {
        Lote anterior = lote("lote-1", 500, null);
        anterior.finalizar();

        Lote nuevo = lote("lote-2", 300, anterior);

        assertThat(anterior.estaActivo()).isFalse();
        assertThat(nuevo.estaActivo()).isTrue();
    }

    @Test
    void unLoteFinalizadoNoSeFinalizaDosVeces() {
        Lote finalizado = lote("lote-1", 500, null);
        finalizado.finalizar();

        assertThatThrownBy(finalizado::finalizar).isInstanceOf(LoteYaFinalizadoException.class);
    }

    @Test
    void unLoteSinPoblacionOConEdadNegativaNoSeCrea() {
        assertThatThrownBy(() -> lote("lote-1", 0, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Lote.builder().id("l").alojadoEn(galpon, null).poblacionInicial(10).edadSemanas(-1)
                .build()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elGalponPasaAEnRiesgoSanitarioYVuelveAActivo_F08() {
        galpon.marcarEnRiesgoSanitario();
        assertThat(galpon.getEstado()).isEqualTo(EstadoGalpon.EN_RIESGO_SANITARIO);

        galpon.restablecer();
        assertThat(galpon.getEstado()).isEqualTo(EstadoGalpon.ACTIVO);
    }

    @Test
    void soloUnOperarioSeAsignaAUnGalpon() {
        Usuario operario = new Usuario("u1", "operario", "Pedro", "x", Rol.OPERARIO, true);
        Usuario veterinario = new Usuario("u2", "veterinario", "Valeria", "x", Rol.VETERINARIO, true);

        operario.asignarGalpon("galpon-1");

        assertThat(operario.getGalponAsignadoId()).isEqualTo("galpon-1");
        assertThatThrownBy(() -> veterinario.asignarGalpon("galpon-1"))
                .isInstanceOf(UsuarioNoEsWorkerException.class);
    }
}
