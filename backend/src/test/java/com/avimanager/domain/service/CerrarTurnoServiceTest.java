package com.avimanager.domain.service;

import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.EstadoReporte;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del caso de uso F-01/F-02 usando solo dobles de prueba de los
 * puertos de salida: no se levanta Spring ni ningun framework, lo cual
 * confirma que el dominio en la arquitectura hexagonal es independiente
 * de la infraestructura.
 */
class CerrarTurnoServiceTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-03-10T08:00:00Z"), ZoneOffset.UTC);

    private FakeLoteRepository loteRepository;
    private FakeReporteDiarioRepository reporteRepository;
    private FakeAlertaSanitariaRepository alertaRepository;
    private FakeNotificadorAlertaSanitaria notificador;
    private CerrarTurnoService service;

    @BeforeEach
    void setUp() {
        loteRepository = new FakeLoteRepository();
        reporteRepository = new FakeReporteDiarioRepository();
        alertaRepository = new FakeAlertaSanitariaRepository();
        notificador = new FakeNotificadorAlertaSanitaria();
        service = new CerrarTurnoService(loteRepository, reporteRepository, alertaRepository, notificador, RELOJ_FIJO);

        loteRepository.agregar(new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO));
    }

    @Test
    void bloqueaElCierreSiFaltanCamposObligatorios_RN05() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-1", null, 3, "jadeo", 10, null);

        CerrarTurnoResult resultado = service.cerrarTurno(comando);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.CAMPOS_INCOMPLETOS);
        assertThat(resultado.getCamposFaltantes()).containsExactly("consumoAlimentoKg");
        assertThat(reporteRepository.guardados).isEmpty();
    }

    @Test
    void exigeCausaProbableCuandoHayBajasRegistradas() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-1", 45.0, 3, null, 10, null);

        CerrarTurnoResult resultado = service.cerrarTurno(comando);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.CAMPOS_INCOMPLETOS);
        assertThat(resultado.getCamposFaltantes()).containsExactly("causaProbableMortalidad");
    }

    @Test
    void cierraElTurnoCuandoLaMortalidadEstaDentroDelUmbral() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-1", 45.0, 5, "jadeo", 12, "sin novedad");

        CerrarTurnoResult resultado = service.cerrarTurno(comando);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.TURNO_CERRADO);
        assertThat(resultado.getReporteDiario().getEstado()).isEqualTo(EstadoReporte.CONSOLIDADO);
        assertThat(resultado.getReporteDiario().getPorcentajeMortalidad()).isEqualTo(1.0);
        assertThat(loteRepository.buscarPorId("lote-1").get().getPoblacionActual()).isEqualTo(495);
        assertThat(loteRepository.buscarPorId("lote-1").get().getEstado()).isEqualTo(EstadoLote.ACTIVO);
        assertThat(alertaRepository.guardadas).isEmpty();
        assertThat(notificador.notificadas).isEmpty();
    }

    @Test
    void bloqueaElCierreYGeneraAlertaSanitariaCuandoSuperaElUmbral_RN01_RN07() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-1", 45.0, 10, "enfermedad", 12, "brote sospechoso");

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
    }

    @Test
    void fallaSiElLoteNoExiste() {
        CerrarTurnoCommand comando = new CerrarTurnoCommand("lote-inexistente", 45.0, 2, "jadeo", 10, null);

        assertThatThrownBy(() -> service.cerrarTurno(comando))
                .isInstanceOf(LoteNoEncontradoException.class);
    }
}
