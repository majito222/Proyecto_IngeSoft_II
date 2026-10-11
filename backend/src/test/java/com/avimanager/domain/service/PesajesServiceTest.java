package com.avimanager.domain.service;

import com.avimanager.domain.event.PesajeFueraDeRango;
import com.avimanager.domain.exception.GalponNoAsignadoException;
import com.avimanager.domain.exception.PesajeNoEncontradoException;
import com.avimanager.domain.exception.PesajeYaRegistradoException;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.EstadoPesaje;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.ReferenciaPesaje;
import com.avimanager.domain.port.in.RegistrarPesajeCommand;
import com.avimanager.domain.port.in.ResumenPesaje;
import com.avimanager.domain.port.in.RevisarPesajeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-07: registrar el pesaje semanal, revisarlo y listarlo, con dobles de prueba
 * y sin Spring.
 */
class PesajesServiceTest {

    // Martes 10 de marzo de 2026.
    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-03-10T07:00:00Z"), ZoneOffset.UTC);

    private FakePesajeRepository pesajes;
    private FakePublicadorEventos publicador;
    private FakeLoteRepository lotes;
    private RegistrarPesajeService registrar;
    private RevisarPesajeService revisar;
    private ConsultarPesajesService consultar;

    @BeforeEach
    void setUp() {
        pesajes = new FakePesajeRepository();
        publicador = new FakePublicadorEventos();
        lotes = new FakeLoteRepository();
        FakeGalponRepository galpones = new FakeGalponRepository();
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
        registrar = new RegistrarPesajeService(usuarios, galpones, lotes, pesajes, publicador, RELOJ_FIJO);
        revisar = new RevisarPesajeService(pesajes, galpones, usuarios, RELOJ_FIJO);
        consultar = new ConsultarPesajesService(pesajes, galpones, usuarios);

        galpones.guardar(new Galpon("galpon-3", "Galpón 3", 700));
        // Semana 2: peso estandar 120 g.
        lotes.agregar(new Lote("lote-3", "galpon-3", 600, 600, 2, EstadoLote.ACTIVO));
        usuarios.guardar(new Usuario("u1", "operario3", "Óscar Operario", "x", Rol.OPERARIO, true, "galpon-3"));
        usuarios.guardar(new Usuario("u2", "zootecnista", "Zoe Zootecnista", "x", Rol.ZOOTECNISTA, true));
        usuarios.guardar(new Usuario("u3", "admin", "Ana Administradora", "x", Rol.ADMINISTRADOR, true));
    }

    private ResumenPesaje pesar(double gramos) {
        return registrar.registrar(new RegistrarPesajeCommand("operario3", gramos, 30));
    }

    @Test
    void laReferenciaMuestraElPesoEstandarDeLaSemanaDelLote() {
        ReferenciaPesaje referencia = registrar.referenciaDelWorker("operario3");

        assertThat(referencia.getGalponNombre()).isEqualTo("Galpón 3");
        assertThat(referencia.getLoteId()).isEqualTo("lote-3");
        assertThat(referencia.getPesoEstandarGramos()).isEqualTo(120.0);
        assertThat(referencia.getPesajeDeLaSemana()).isNull();

        pesar(118);
        assertThat(registrar.referenciaDelWorker("operario3").getPesajeDeLaSemana()).isNotNull();
    }

    @Test
    void unPesajeDentroDeRangoNoAvisaAlZootecnista() {
        ResumenPesaje resumen = pesar(118);

        assertThat(resumen.getPesaje().getEstado()).isEqualTo(EstadoPesaje.DENTRO_DE_RANGO);
        assertThat(resumen.getRegistradoPorNombre()).isEqualTo("Óscar Operario");
        assertThat(pesajes.guardados).hasSize(1);
        assertThat(publicador.publicados).isEmpty();
    }

    @Test
    void unPesajeFueraDeRangoQuedaPendienteYPublicaElEvento_RN02() {
        ResumenPesaje resumen = pesar(113);

        assertThat(resumen.getPesaje().getEstado()).isEqualTo(EstadoPesaje.PENDIENTE_REVISION);
        assertThat(resumen.getPesaje().getDesviacionPorcentaje()).isEqualTo(-5.83);
        assertThat(publicador.publicados).singleElement().isInstanceOf(PesajeFueraDeRango.class);
        assertThat(((PesajeFueraDeRango) publicador.publicados.get(0)).getPesaje())
                .isSameAs(resumen.getPesaje());
    }

    @Test
    void unLoteSePesaUnaSolaVezPorSemana() {
        // El lunes de la misma semana ya se peso.
        pesajes.guardar(new Pesaje("previo", lotes.buscarPorId("lote-3").orElseThrow(), 118, 30, "operario3",
                LocalDateTime.of(2026, 3, 9, 7, 0)));

        assertThatThrownBy(() -> pesar(118))
                .isInstanceOf(PesajeYaRegistradoException.class)
                .hasMessageContaining("ya se pesó esta semana");
    }

    @Test
    void elPesajeDeLaSemanaPasadaNoImpidePesarEstaSemana() {
        // Domingo 8 de marzo: semana anterior.
        pesajes.guardar(new Pesaje("previo", lotes.buscarPorId("lote-3").orElseThrow(), 118, 30, "operario3",
                LocalDateTime.of(2026, 3, 8, 7, 0)));

        assertThat(pesar(118).getPesaje().getEstado()).isEqualTo(EstadoPesaje.DENTRO_DE_RANGO);
    }

    @Test
    void sinGalponAsignadoNoSePuedePesar() {
        assertThatThrownBy(() -> registrar.registrar(new RegistrarPesajeCommand("admin", 118.0, 30)))
                .isInstanceOf(GalponNoAsignadoException.class);
    }

    @Test
    void elPesoYLaMuestraSonObligatorios() {
        assertThatThrownBy(() -> registrar.registrar(new RegistrarPesajeCommand("operario3", null, 30)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registrar.registrar(new RegistrarPesajeCommand("operario3", 118.0, null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(pesajes.guardados).isEmpty();
    }

    @Test
    void elZootecnistaApruebaElAjusteDeRacion() {
        String id = pesar(113).getPesaje().getId();

        ResumenPesaje revisado = revisar.revisar(new RevisarPesajeCommand(id, "zootecnista", true));

        assertThat(revisado.getPesaje().getEstado()).isEqualTo(EstadoPesaje.AJUSTE_APROBADO);
        assertThat(revisado.getRevisadoPorNombre()).isEqualTo("Zoe Zootecnista");
        assertThat(revisado.getPesaje().getFechaRevision()).isEqualTo(LocalDateTime.of(2026, 3, 10, 7, 0));
    }

    @Test
    void elZootecnistaMantieneLaRacion() {
        String id = pesar(113).getPesaje().getId();

        ResumenPesaje revisado = revisar.revisar(new RevisarPesajeCommand(id, "zootecnista", false));

        assertThat(revisado.getPesaje().getEstado()).isEqualTo(EstadoPesaje.AJUSTE_DESCARTADO);
    }

    @Test
    void revisarUnPesajeInexistenteFalla() {
        assertThatThrownBy(() -> revisar.revisar(new RevisarPesajeCommand("no-existe", "zootecnista", true)))
                .isInstanceOf(PesajeNoEncontradoException.class);
    }

    @Test
    void elListadoMuestraPrimeroLosPendientesYLuegoLosMasRecientes() {
        Lote lote = lotes.buscarPorId("lote-3").orElseThrow();
        pesajes.guardar(new Pesaje("viejo-en-rango", lote, 118, 30, "operario3", LocalDateTime.of(2026, 2, 20, 7, 0)));
        pesajes.guardar(new Pesaje("pendiente", lote, 100, 30, "operario3", LocalDateTime.of(2026, 2, 27, 7, 0)));
        pesajes.guardar(new Pesaje("reciente-en-rango", lote, 120, 30, "operario3", LocalDateTime.of(2026, 3, 6, 7, 0)));

        List<ResumenPesaje> listado = consultar.listarPesajes();

        assertThat(listado).extracting(r -> r.getPesaje().getId())
                .containsExactly("pendiente", "reciente-en-rango", "viejo-en-rango");
        assertThat(listado.get(0).getGalponNombre()).isEqualTo("Galpón 3");
    }
}
