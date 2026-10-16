package com.avimanager.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InsumoTest {

    private Insumo alimento(double stock) {
        return new Insumo("alimento", "Alimento balanceado", TipoInsumo.ALIMENTO, "kg", 10000, stock);
    }

    @Test
    void descontarRestaLoConsumidoDelStock() {
        Insumo insumo = alimento(5030);

        insumo.descontar(45);

        assertThat(insumo.getStock()).isEqualTo(4985);
        assertThat(insumo.porcentajeDisponible()).isEqualTo(49.85);
        assertThat(insumo.esCritico()).isTrue();
    }

    @Test
    void siElConsumoSuperaElStockQuedaEnCero() {
        Insumo insumo = alimento(30);

        insumo.descontar(45);

        assertThat(insumo.getStock()).isZero();
    }

    @Test
    void noSeDescuentaUnaCantidadNegativa() {
        assertThatThrownBy(() -> alimento(100).descontar(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void laCapacidadDebeSerPositivaYElStockNoNegativo() {
        assertThatThrownBy(() -> new Insumo("x", "X", TipoInsumo.SANIDAD, "dosis", 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Insumo("x", "X", TipoInsumo.SANIDAD, "dosis", 10, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
