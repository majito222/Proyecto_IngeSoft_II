package com.avimanager.domain.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RN-02: tabla de peso estandar y tolerancia de +-5%.
 */
class PoliticaPesoTest {

    @Test
    void elPesoEstandarSaleDeLaTablaPorSemana() {
        assertThat(PoliticaPeso.pesoEstandarGramos(1)).isEqualTo(70.0);
        assertThat(PoliticaPeso.pesoEstandarGramos(20)).isEqualTo(1600.0);
        assertThat(PoliticaPeso.pesoEstandarGramos(25)).as("peso adulto").isEqualTo(1900.0);
        assertThat(PoliticaPeso.pesoEstandarGramos(80)).isEqualTo(1900.0);
    }

    @Test
    void fueraDeLaTablaEsUnError() {
        assertThatThrownBy(() -> PoliticaPeso.pesoEstandarGramos(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PoliticaPeso.pesoEstandarGramos(81)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void laDesviacionEsNegativaSiElPesoEstaPorDebajo() {
        assertThat(PoliticaPeso.calcularDesviacion(113, 120)).isEqualTo(-5.83);
        assertThat(PoliticaPeso.calcularDesviacion(127, 120)).isEqualTo(5.83);
    }

    @Test
    void elLimiteDel5PorcientoEsEstricto() {
        assertThat(PoliticaPeso.fueraDeRango(5.0)).isFalse();
        assertThat(PoliticaPeso.fueraDeRango(-5.0)).isFalse();
        assertThat(PoliticaPeso.fueraDeRango(5.01)).isTrue();
        assertThat(PoliticaPeso.fueraDeRango(-5.01)).isTrue();
    }
}
