package com.avimanager.domain.model;

import com.avimanager.domain.exception.TurnoNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ciclo de vida de la entidad Turno (F-10.1).
 */
class TurnoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 3, 10);

    private Turno turno;
    private ReporteDiario reporte;

    @BeforeEach
    void setUp() {
        turno = new Turno("t-1", "operario", "lote-1", "galpon-1", HOY, HOY.atTime(6, 0));
        reporte = new ReporteDiario("r-1", "lote-1", HOY, 45.0, 5, "jadeo", 12, null);
        reporte.registrarPorcentajeMortalidad(1.0);
    }

    @Test
    void unTurnoNuevoEmpiezaAbiertoYSinCierre() {
        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.ABIERTO);
        assertThat(turno.estaAbierto()).isTrue();
        assertThat(turno.getHoraCierre()).isNull();
    }

    @Test
    void cerrarRegistraElReporteLaMortalidadYLaHora() {
        LocalDateTime ahora = HOY.atTime(14, 0);

        turno.cerrar(reporte, ahora);

        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.CERRADO);
        assertThat(turno.getHoraCierre()).isEqualTo(ahora);
        assertThat(turno.getReporteDiarioId()).isEqualTo("r-1");
        assertThat(turno.getPorcentajeMortalidad()).isEqualTo(1.0);
    }

    @Test
    void bloquearPorAlertaGuardaLaAlertaYNoCierraElTurno() {
        AlertaSanitaria alerta = new AlertaSanitaria("a-1", "lote-1", "r-1", HOY.atTime(13, 0), 2.0);

        turno.bloquearPorAlertaSanitaria(reporte, alerta);

        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.BLOQUEADO_ALERTA_SANITARIA);
        assertThat(turno.getAlertaSanitariaId()).isEqualTo("a-1");
        assertThat(turno.getHoraCierre()).isNull();
    }

    @Test
    void unTurnoQueNoEstaAbiertoNoSePuedeVolverACerrar() {
        turno.cerrar(reporte, HOY.atTime(14, 0));

        assertThatThrownBy(() -> turno.cerrar(reporte, HOY.atTime(15, 0)))
                .isInstanceOf(TurnoNoDisponibleException.class);
        assertThatThrownBy(() -> turno.bloquearPorAlertaSanitaria(reporte,
                new AlertaSanitaria("a-1", "lote-1", "r-1", HOY.atTime(15, 0), 2.0)))
                .isInstanceOf(TurnoNoDisponibleException.class);
    }

    @Test
    void unTurnoBloqueadoSeCierraConAlertaCuandoSeAtiendeLaAlerta_F025() {
        turno.bloquearPorAlertaSanitaria(reporte, new AlertaSanitaria("a-1", "lote-1", "r-1", HOY.atTime(13, 0), 2.0));

        turno.cerrarConAlertaAtendida(HOY.atTime(16, 0));

        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.CERRADO_CON_ALERTA);
        assertThat(turno.getHoraCierre()).isEqualTo(HOY.atTime(16, 0));
    }

    @Test
    void unTurnoAbiertoNoSePuedeCerrarConAlerta_F025() {
        assertThatThrownBy(() -> turno.cerrarConAlertaAtendida(HOY.atTime(16, 0)))
                .isInstanceOf(TurnoNoDisponibleException.class);
    }
}
