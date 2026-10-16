package com.avimanager.domain.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** RN-03: el umbral de Stock Critico es estrictamente menor al 50% de la capacidad. */
class PoliticaStockTest {

    @Test
    void bajoEl50PorCientoEsCritico() {
        assertThat(PoliticaStock.esCritico(4900, 10000)).isTrue();
        assertThat(PoliticaStock.esCritico(4999.9, 10000)).isTrue();
    }

    @Test
    void justoEnEl50PorCientoTodaviaNoEsCritico() {
        assertThat(PoliticaStock.esCritico(5000, 10000)).isFalse();
        assertThat(PoliticaStock.esCritico(150, 300)).isFalse();
    }

    @Test
    void cruzaElUmbralSoloAlPasarDeNoCriticoACritico() {
        assertThat(PoliticaStock.cruzaUmbralCritico(5100, 4900, 10000)).isTrue();
        assertThat(PoliticaStock.cruzaUmbralCritico(5100, 5000, 10000)).isFalse();
        assertThat(PoliticaStock.cruzaUmbralCritico(4900, 4800, 10000)).isFalse();
    }

    @Test
    void calculaElPorcentajeDisponible() {
        assertThat(PoliticaStock.porcentajeDisponible(2500, 10000)).isEqualTo(25.0);
    }
}
