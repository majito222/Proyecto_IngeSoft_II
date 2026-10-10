package com.avimanager.domain.service.racion;

import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Lote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-06.3: la estrategia de tabla estandar devuelve los gramos por ave de la
 * tabla de nutricion segun la semana de edad, y rechaza edades fuera de ella.
 */
class RacionTablaEstandarTest {

    private final RacionTablaEstandar tabla = new RacionTablaEstandar();

    private static Lote loteDe(int semanas) {
        return new Lote("lote-x", "galpon-x", 1000, 1000, semanas, EstadoLote.ACTIVO);
    }

    @ParameterizedTest(name = "semana {0} -> {1} g por ave")
    @CsvSource({
            "1,  10",
            "3,  23",
            "5,  35",
            "19, 95",
            "20, 110",
            "80, 110",
    })
    void devuelveLosGramosDeLaTablaSegunLaEdad(int semanas, double gramosEsperados) {
        assertThat(tabla.gramosPorAve(loteDe(semanas))).isEqualTo(gramosEsperados);
    }

    @Test
    void rechazaEdadesFueraDeLaTabla() {
        assertThatThrownBy(() -> tabla.gramosPorAve(loteDe(0)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("fuera de la tabla");
        assertThatThrownBy(() -> tabla.gramosPorAve(loteDe(81)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tieneUnNombreLegibleParaMostrarDeDondeSaleLaRacion() {
        assertThat(tabla.nombre()).isEqualTo("Tabla de nutrición estándar");
    }
}
