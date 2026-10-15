package com.avimanager.domain.service.mantenimiento;

import com.avimanager.domain.model.EstadoOrden;
import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.model.PrioridadOrden;
import com.avimanager.domain.model.TipoDano;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-08.1: la fabrica asigna la prioridad segun el tipo de dano y el plazo de 12 h (RN-04).
 */
class FabricaOrdenMantenimientoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 3, 10, 22, 30);
    private final FabricaOrdenMantenimiento fabrica = new FabricaOrdenMantenimiento();

    @ParameterizedTest(name = "{0} -> prioridad {1}")
    @CsvSource({
            "ROTURA_MALLA,   ALTA",
            "DANO_TECHO,     ALTA",
            "CORTINA_DANADA, MEDIA",
            "OTRO,           MEDIA",
    })
    void laPrioridadDependeDelTipoDeDano(TipoDano tipo, PrioridadOrden prioridad) {
        assertThat(fabrica.crear("galpon-1", tipo, "Daño", "operario", AHORA).getPrioridad()).isEqualTo(prioridad);
    }

    @Test
    void laOrdenNaceAbiertaConPlazoDe12Horas() {
        OrdenMantenimiento orden = fabrica.crear("galpon-1", TipoDano.ROTURA_MALLA, "  Malla rota  ", "operario", AHORA);

        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.ABIERTA);
        assertThat(orden.getFechaReporte()).isEqualTo(AHORA);
        assertThat(orden.getFechaLimite()).as("cruza la medianoche").isEqualTo(LocalDateTime.of(2026, 3, 11, 10, 30));
        assertThat(orden.getDescripcion()).isEqualTo("Malla rota");
        assertThat(orden.getGalponId()).isEqualTo("galpon-1");
        assertThat(orden.getReportadoPor()).isEqualTo("operario");
        assertThat(orden.getId()).isNotBlank();
    }

    @Test
    void elTipoYLaDescripcionSonObligatorios() {
        assertThatThrownBy(() -> fabrica.crear("galpon-1", null, "Daño", "operario", AHORA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fabrica.crear("galpon-1", TipoDano.OTRO, " ", "operario", AHORA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
