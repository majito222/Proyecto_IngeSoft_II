package com.avimanager.domain.model;

import com.avimanager.domain.exception.TurnoNoDisponibleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Maquina de estados del turno (patron State): cada estado solo admite sus
 * transiciones y rechaza las demas.
 */
class EstadoTurnoTest {

    @Test
    void abiertoSePuedeCerrarOBloquearPorAlerta() {
        assertThat(EstadoTurno.ABIERTO.admiteCierre()).isTrue();
        assertThat(EstadoTurno.ABIERTO.cerrar()).isEqualTo(EstadoTurno.CERRADO);
        assertThat(EstadoTurno.ABIERTO.bloquearPorAlertaSanitaria()).isEqualTo(EstadoTurno.BLOQUEADO_ALERTA_SANITARIA);
    }

    @Test
    void unTurnoBloqueadoSoloSeCierraCuandoSeAtiendeLaAlerta() {
        assertThat(EstadoTurno.BLOQUEADO_ALERTA_SANITARIA.cerrarConAlertaAtendida())
                .isEqualTo(EstadoTurno.CERRADO_CON_ALERTA);
        assertThatThrownBy(EstadoTurno.BLOQUEADO_ALERTA_SANITARIA::cerrar)
                .isInstanceOf(TurnoNoDisponibleException.class)
                .hasMessage("No se puede cerrar un turno en estado BLOQUEADO_ALERTA_SANITARIA");
    }

    @Test
    void soloLosTurnosCerradosSePuedenAprobar() {
        assertThat(EstadoTurno.CERRADO.aprobar()).isEqualTo(EstadoTurno.APROBADO);
        assertThat(EstadoTurno.CERRADO_CON_ALERTA.aprobar()).isEqualTo(EstadoTurno.APROBADO);
        assertThatThrownBy(EstadoTurno.ABIERTO::aprobar).isInstanceOf(TurnoNoDisponibleException.class);
        assertThatThrownBy(EstadoTurno.BLOQUEADO_ALERTA_SANITARIA::aprobar).isInstanceOf(TurnoNoDisponibleException.class);
    }

    @Test
    void unTurnoAprobadoNoAdmiteNingunaTransicion() {
        EstadoTurno aprobado = EstadoTurno.APROBADO;

        assertThat(aprobado.admiteCierre()).isFalse();
        assertThatThrownBy(aprobado::cerrar).isInstanceOf(TurnoNoDisponibleException.class);
        assertThatThrownBy(aprobado::bloquearPorAlertaSanitaria).isInstanceOf(TurnoNoDisponibleException.class);
        assertThatThrownBy(aprobado::cerrarConAlertaAtendida).isInstanceOf(TurnoNoDisponibleException.class);
        assertThatThrownBy(aprobado::aprobar).isInstanceOf(TurnoNoDisponibleException.class);
    }

    @Test
    void soloElTurnoAbiertoAdmiteElCierreDelWorker() {
        for (EstadoTurno estado : EstadoTurno.values()) {
            assertThat(estado.admiteCierre()).as(estado.name()).isEqualTo(estado == EstadoTurno.ABIERTO);
        }
    }
}
