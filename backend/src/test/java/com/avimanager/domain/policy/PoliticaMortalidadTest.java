package com.avimanager.domain.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaMortalidadTest {

    @Test
    void calculaElPorcentajeSegunLaFormulaRN07() {
        double porcentaje = PoliticaMortalidad.calcularPorcentaje(6, 500);

        assertThat(porcentaje).isEqualTo(1.2);
    }

    @Test
    void exactamenteElUmbralNoSeConsideraQueLoSupera() {
        double porcentaje = PoliticaMortalidad.calcularPorcentaje(15, 1000); // 1.5%

        assertThat(PoliticaMortalidad.superaUmbralCritico(porcentaje)).isFalse();
    }

    @Test
    void masDelUmbralSiLoSupera() {
        double porcentaje = PoliticaMortalidad.calcularPorcentaje(16, 1000); // 1.6%

        assertThat(PoliticaMortalidad.superaUmbralCritico(porcentaje)).isTrue();
    }

    @Test
    void rechazaPoblacionInicialNoPositiva() {
        assertThatThrownBy(() -> PoliticaMortalidad.calcularPorcentaje(1, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaAvesMuertasNegativas() {
        assertThatThrownBy(() -> PoliticaMortalidad.calcularPorcentaje(-1, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
