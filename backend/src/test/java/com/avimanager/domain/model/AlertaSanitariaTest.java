package com.avimanager.domain.model;

import com.avimanager.domain.exception.AlertaYaAtendidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-02.5: una alerta se atiende una sola vez y con diagnostico y tratamiento.
 */
class AlertaSanitariaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 3, 10, 15, 0);

    private final AlertaSanitaria alerta =
            new AlertaSanitaria("a-1", "lote-1", "r-1", LocalDateTime.of(2026, 3, 10, 13, 0), 2.0);

    @Test
    void naceComoPendiente() {
        assertThat(alerta.getEstado()).isEqualTo(EstadoAlerta.PENDIENTE);
        assertThat(alerta.estaPendiente()).isTrue();
    }

    @Test
    void atenderRegistraDiagnosticoTratamientoQuienYCuando() {
        alerta.atender("  Bronquitis infecciosa ", "Vacuna de refuerzo", "veterinario", AHORA);

        assertThat(alerta.getEstado()).isEqualTo(EstadoAlerta.ATENDIDA);
        assertThat(alerta.getDiagnostico()).isEqualTo("Bronquitis infecciosa");
        assertThat(alerta.getTratamiento()).isEqualTo("Vacuna de refuerzo");
        assertThat(alerta.getAtendidaPor()).isEqualTo("veterinario");
        assertThat(alerta.getFechaAtencion()).isEqualTo(AHORA);
    }

    @Test
    void noSePuedeAtenderDosVeces() {
        alerta.atender("Bronquitis", "Vacuna", "veterinario", AHORA);

        assertThatThrownBy(() -> alerta.atender("Otro", "Otro", "admin", AHORA))
                .isInstanceOf(AlertaYaAtendidaException.class);
        assertThat(alerta.getAtendidaPor()).isEqualTo("veterinario");
    }

    @Test
    void diagnosticoYTratamientoSonObligatorios() {
        assertThatThrownBy(() -> alerta.atender(" ", "Vacuna", "veterinario", AHORA))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("diagnóstico");
        assertThatThrownBy(() -> alerta.atender("Bronquitis", null, "veterinario", AHORA))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("tratamiento");
        assertThat(alerta.estaPendiente()).isTrue();
    }
}
