package com.avimanager.domain.service;

import com.avimanager.domain.event.AlertaSanitariaAtendida;
import com.avimanager.domain.exception.AlertaNoEncontradaException;
import com.avimanager.domain.exception.AlertaYaAtendidaException;
import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.EstadoAlerta;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.EstadoReporte;
import com.avimanager.domain.model.EstadoTurno;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.AtenderAlertaCommand;
import com.avimanager.domain.port.in.ResumenAlerta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-02.5 (atender la alerta y cerrar el turno bloqueado) y F-02.6 (listado de
 * alertas), con dobles de prueba y sin Spring.
 */
class AlertasServiceTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-03-10T15:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate HOY = LocalDate.of(2026, 3, 10);

    private FakeAlertaSanitariaRepository alertaRepository;
    private FakeTurnoRepository turnoRepository;
    private FakeReporteDiarioRepository reporteRepository;
    private FakeLoteRepository loteRepository;
    private FakePublicadorEventos publicador;
    private AtenderAlertaSanitariaService atenderAlerta;
    private ConsultarAlertasService consultarAlertas;
    private Turno turno;

    @BeforeEach
    void setUp() {
        alertaRepository = new FakeAlertaSanitariaRepository();
        turnoRepository = new FakeTurnoRepository();
        reporteRepository = new FakeReporteDiarioRepository();
        loteRepository = new FakeLoteRepository();
        publicador = new FakePublicadorEventos();
        FakeGalponRepository galpones = new FakeGalponRepository();
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
        atenderAlerta = new AtenderAlertaSanitariaService(alertaRepository, turnoRepository, reporteRepository,
                loteRepository, galpones, usuarios, publicador, RELOJ_FIJO);
        consultarAlertas = new ConsultarAlertasService(alertaRepository, turnoRepository, loteRepository,
                galpones, usuarios);

        galpones.guardar(new Galpon("galpon-2", "Galpón 2", 900));
        usuarios.guardar(new Usuario("u1", "operario2", "Olga Operaria", "x", Rol.OPERARIO, true, "galpon-2"));
        usuarios.guardar(new Usuario("u2", "veterinario", "Valeria Veterinaria", "x", Rol.VETERINARIO, true));
        loteRepository.agregar(new Lote("lote-2", "galpon-2", 800, 800, 5, EstadoLote.EN_OBSERVACION));
        turno = turnoBloqueado("a-1", "r-1", 16, HOY.atTime(13, 0));
    }

    /** Arma un turno bloqueado por alerta, tal como lo deja ReglaMortalidad. */
    private Turno turnoBloqueado(String alertaId, String reporteId, int bajas, LocalDateTime fechaAlerta) {
        ReporteDiario reporte = new ReporteDiario(reporteId, "lote-2", HOY, 45.0, bajas, "enfermedad", 12, null);
        reporte.bloquearPorAlertaSanitaria(bajas / 8.0);
        reporteRepository.guardar(reporte);
        AlertaSanitaria alerta = new AlertaSanitaria(alertaId, "lote-2", reporteId, fechaAlerta, bajas / 8.0);
        alertaRepository.guardar(alerta);
        Turno t = new Turno("t-" + alertaId, "operario2", "lote-2", "galpon-2", HOY, HOY.atTime(6, 0));
        t.bloquearPorAlertaSanitaria(reporte, alerta);
        return turnoRepository.guardar(t);
    }

    private AtenderAlertaCommand comando(boolean liberarLote) {
        return new AtenderAlertaCommand("a-1", "veterinario", "Bronquitis infecciosa", "Vacuna de refuerzo", liberarLote);
    }

    @Test
    void atenderLaAlertaCierraElTurnoConAlertaYConsolidaElReporte() {
        ResumenAlerta resumen = atenderAlerta.atender(comando(false));

        assertThat(resumen.getAlerta().getEstado()).isEqualTo(EstadoAlerta.ATENDIDA);
        assertThat(resumen.getAtendidaPorNombre()).isEqualTo("Valeria Veterinaria");
        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.CERRADO_CON_ALERTA);
        assertThat(turno.getHoraCierre()).isEqualTo(LocalDateTime.of(2026, 3, 10, 15, 0));
        assertThat(reporteRepository.buscarPorId("r-1").get().getEstado()).isEqualTo(EstadoReporte.CONSOLIDADO);

        Lote lote = loteRepository.buscarPorId("lote-2").get();
        assertThat(lote.getPoblacionActual()).as("se descuentan las bajas del reporte").isEqualTo(784);
        assertThat(lote.getEstado()).as("el lote sigue en observacion si no se libera").isEqualTo(EstadoLote.EN_OBSERVACION);
    }

    @Test
    void elVeterinarioPuedeLiberarElLoteDeObservacion() {
        atenderAlerta.atender(comando(true));

        assertThat(loteRepository.buscarPorId("lote-2").get().getEstado()).isEqualTo(EstadoLote.ACTIVO);
    }

    @Test
    void publicaAlertaSanitariaAtendidaParaAvisarAlAdministrador() {
        atenderAlerta.atender(comando(false));

        assertThat(publicador.publicados).singleElement().isInstanceOfSatisfying(AlertaSanitariaAtendida.class,
                e -> assertThat(e.getTurno().getEstado()).isEqualTo(EstadoTurno.CERRADO_CON_ALERTA));
    }

    @Test
    void unaAlertaAtendidaNoSePuedeVolverAAtenderNiDescuentaDosVeces() {
        atenderAlerta.atender(comando(false));

        assertThatThrownBy(() -> atenderAlerta.atender(comando(true))).isInstanceOf(AlertaYaAtendidaException.class);
        assertThat(loteRepository.buscarPorId("lote-2").get().getPoblacionActual()).isEqualTo(784);
    }

    @Test
    void sinDiagnosticoNoSeTocaNada() {
        AtenderAlertaCommand sinDiagnostico = new AtenderAlertaCommand("a-1", "veterinario", "", "Vacuna", true);

        assertThatThrownBy(() -> atenderAlerta.atender(sinDiagnostico)).isInstanceOf(IllegalArgumentException.class);
        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.BLOQUEADO_ALERTA_SANITARIA);
        assertThat(loteRepository.buscarPorId("lote-2").get().getPoblacionActual()).isEqualTo(800);
        assertThat(publicador.publicados).isEmpty();
    }

    @Test
    void fallaSiLaAlertaNoExiste() {
        AtenderAlertaCommand inexistente = new AtenderAlertaCommand("no-existe", "veterinario", "x", "y", false);

        assertThatThrownBy(() -> atenderAlerta.atender(inexistente)).isInstanceOf(AlertaNoEncontradaException.class);
    }

    @Test
    void elListadoMuestraPrimeroLaMasRecienteConLosNombres() {
        turnoBloqueado("a-0", "r-0", 12, HOY.minusDays(1).atTime(13, 0));

        List<ResumenAlerta> alertas = consultarAlertas.listarAlertas();

        assertThat(alertas).extracting(r -> r.getAlerta().getId()).containsExactly("a-1", "a-0");
        assertThat(alertas.get(0).getGalponNombre()).isEqualTo("Galpón 2");
        assertThat(alertas.get(0).getResponsableTurnoNombre()).isEqualTo("Olga Operaria");
        assertThat(alertas.get(0).getAtendidaPorNombre()).isNull();
    }
}
