package com.avimanager.domain.service;

import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.exception.TurnoAjenoException;
import com.avimanager.domain.exception.TurnoNoDisponibleException;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.EstadoReporte;
import com.avimanager.domain.model.EstadoTurno;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del caso de uso F-01/F-02 usando solo dobles de prueba de los
 * puertos de salida: no se levanta Spring ni ningun framework, lo cual
 * confirma que el dominio en la arquitectura hexagonal es independiente
 * de la infraestructura. Desde F-10 tambien verifican el estado del turno.
 */
class CerrarTurnoServiceTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-03-10T08:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate HOY = LocalDate.of(2026, 3, 10);

    private FakeLoteRepository loteRepository;
    private FakeReporteDiarioRepository reporteRepository;
    private FakeAlertaSanitariaRepository alertaRepository;
    private FakeNotificadorAlertaSanitaria notificador;
    private FakeTurnoRepository turnoRepository;
    private CerrarTurnoService service;
    private Turno turnoDeHoy;

    @BeforeEach
    void setUp() {
        loteRepository = new FakeLoteRepository();
        reporteRepository = new FakeReporteDiarioRepository();
        alertaRepository = new FakeAlertaSanitariaRepository();
        notificador = new FakeNotificadorAlertaSanitaria();
        turnoRepository = new FakeTurnoRepository();
        service = new CerrarTurnoService(loteRepository, reporteRepository, alertaRepository, notificador,
                turnoRepository, RELOJ_FIJO);

        loteRepository.agregar(new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO));
        turnoDeHoy = turnoRepository.guardar(
                new Turno("turno-1", "operario", "lote-1", "galpon-1", HOY, HOY.atTime(6, 0)));
    }

    @Test
    void bloqueaElCierreSiFaltanCamposObligatorios_RN05() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-1", null, 3, "jadeo", 10, null, "operario");

        CerrarTurnoResult resultado = service.cerrarTurno(comando);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.CAMPOS_INCOMPLETOS);
        assertThat(resultado.getCamposFaltantes()).containsExactly("consumoAlimentoKg");
        assertThat(reporteRepository.guardados).isEmpty();
        assertThat(turnoDeHoy.getEstado())
                .as("con campos faltantes el turno sigue abierto para completarlo")
                .isEqualTo(EstadoTurno.ABIERTO);
    }

    @Test
    void exigeCausaProbableCuandoHayBajasRegistradas() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-1", 45.0, 3, null, 10, null, "operario");

        CerrarTurnoResult resultado = service.cerrarTurno(comando);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.CAMPOS_INCOMPLETOS);
        assertThat(resultado.getCamposFaltantes()).containsExactly("causaProbableMortalidad");
    }

    @Test
    void cierraElTurnoCuandoLaMortalidadEstaDentroDelUmbral() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-1", 45.0, 5, "jadeo", 12, "sin novedad", "operario");

        CerrarTurnoResult resultado = service.cerrarTurno(comando);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.TURNO_CERRADO);
        assertThat(resultado.getReporteDiario().getEstado()).isEqualTo(EstadoReporte.CONSOLIDADO);
        assertThat(resultado.getReporteDiario().getPorcentajeMortalidad()).isEqualTo(1.0);
        assertThat(loteRepository.buscarPorId("lote-1").get().getPoblacionActual()).isEqualTo(495);
        assertThat(loteRepository.buscarPorId("lote-1").get().getEstado()).isEqualTo(EstadoLote.ACTIVO);
        assertThat(alertaRepository.guardadas).isEmpty();
        assertThat(notificador.notificadas).isEmpty();

        assertThat(turnoDeHoy.getEstado()).isEqualTo(EstadoTurno.CERRADO);
        assertThat(turnoDeHoy.getHoraCierre()).isEqualTo(LocalDateTime.of(2026, 3, 10, 8, 0));
        assertThat(turnoDeHoy.getReporteDiarioId()).isEqualTo(resultado.getReporteDiario().getId());
    }

    @Test
    void bloqueaElCierreYGeneraAlertaSanitariaCuandoSuperaElUmbral_RN01_RN07() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-1", 45.0, 10, "enfermedad", 12, "brote sospechoso", "operario");

        CerrarTurnoResult resultado = service.cerrarTurno(comando);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.ALERTA_SANITARIA);
        assertThat(resultado.getReporteDiario().getEstado()).isEqualTo(EstadoReporte.BLOQUEADO_ALERTA_SANITARIA);
        assertThat(resultado.getReporteDiario().getPorcentajeMortalidad()).isEqualTo(2.0);
        assertThat(resultado.getAlertaSanitaria()).isNotNull();

        Lote loteActualizado = loteRepository.buscarPorId("lote-1").get();
        assertThat(loteActualizado.getEstado()).isEqualTo(EstadoLote.EN_OBSERVACION);
        assertThat(loteActualizado.getPoblacionActual())
                .as("la poblacion actual no se descuenta mientras el turno esta bloqueado por la alerta")
                .isEqualTo(500);

        assertThat(alertaRepository.guardadas).hasSize(1);
        assertThat(notificador.notificadas).hasSize(1);

        assertThat(turnoDeHoy.getEstado()).isEqualTo(EstadoTurno.BLOQUEADO_ALERTA_SANITARIA);
        assertThat(turnoDeHoy.getAlertaSanitariaId()).isEqualTo(resultado.getAlertaSanitaria().getId());
        assertThat(turnoDeHoy.getHoraCierre()).as("un turno bloqueado no queda cerrado").isNull();
    }

    @Test
    void noPermiteCerrarSiNoSeHaIniciadoElTurnoDeHoy_F10() {
        loteRepository.agregar(new Lote("lote-2", "galpon-2", 800, 800, 5, EstadoLote.ACTIVO));
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-2", 45.0, 2, "jadeo", 10, null, "operario");

        assertThatThrownBy(() -> service.cerrarTurno(comando))
                .isInstanceOf(TurnoNoDisponibleException.class)
                .hasMessageContaining("iniciar el turno");
    }

    @Test
    void noPermiteCerrarDosVecesElMismoTurno_F10() {
        service.cerrarTurno(new CerrarTurnoCommand("lote-1", 45.0, 2, "jadeo", 10, null, "operario"));

        assertThatThrownBy(() -> service.cerrarTurno(new CerrarTurnoCommand("lote-1", 45.0, 2, "jadeo", 10, null, "operario")))
                .isInstanceOf(TurnoNoDisponibleException.class)
                .hasMessageContaining("CERRADO");
        assertThat(reporteRepository.guardados).hasSize(1);
    }

    @Test
    void soloElResponsablePuedeCerrarSuTurnoNiSiquieraElAdministrador_F10() {
        for (String otro : new String[]{"operario2", "admin"}) {
            CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-1", 45.0, 2, "jadeo", 10, null, otro);

            assertThatThrownBy(() -> service.cerrarTurno(comando))
                    .isInstanceOf(TurnoAjenoException.class)
                    .hasMessageContaining("operario");
        }
        assertThat(turnoDeHoy.getEstado()).isEqualTo(EstadoTurno.ABIERTO);
        assertThat(reporteRepository.guardados).isEmpty();
    }

    @Test
    void fallaSiElLoteNoExiste() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-inexistente", 45.0, 2, "jadeo", 10, null, "operario");

        assertThatThrownBy(() -> service.cerrarTurno(comando))
                .isInstanceOf(LoteNoEncontradoException.class);
    }
}
