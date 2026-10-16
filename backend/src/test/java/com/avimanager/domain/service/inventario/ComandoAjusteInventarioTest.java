package com.avimanager.domain.service.inventario;

import com.avimanager.domain.model.ActaAjuste;
import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.model.TipoInsumo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-05.3: el patron Command. El comando guarda los datos del ajuste, no toca el
 * insumo hasta que se ejecuta, y al ejecutarse devuelve el acta de auditoria.
 */
class ComandoAjusteInventarioTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 3, 10, 9, 30);

    private final Insumo viruta = new Insumo("viruta", "Viruta para cama", TipoInsumo.MATERIAL_CAMA, "bultos", 300, 120);

    private ComandoInventario comando(Double contado, String justificacion) {
        return new ComandoAjusteInventario("acta-1", viruta, contado, justificacion, "admin", "Ana Administradora",
                AHORA);
    }

    @Test
    void crearElComandoNoCambiaElStockHastaQueSeEjecuta() {
        ComandoInventario ajuste = comando(112.0, "Conteo semanal: 8 bultos mojados");

        assertThat(viruta.getStock()).isEqualTo(120);

        ajuste.ejecutar();

        assertThat(viruta.getStock()).isEqualTo(112);
    }

    @Test
    void alEjecutarseDevuelveElActaConLaAuditoriaCompleta() {
        ActaAjuste acta = comando(112.0, "  Conteo semanal: 8 bultos mojados  ").ejecutar();

        assertThat(acta.getId()).isEqualTo("acta-1");
        assertThat(acta.getInsumoId()).isEqualTo("viruta");
        assertThat(acta.getStockSistema()).isEqualTo(120);
        assertThat(acta.getStockContado()).isEqualTo(112);
        assertThat(acta.getDiferencia()).isEqualTo(-8);
        assertThat(acta.getJustificacion()).isEqualTo("Conteo semanal: 8 bultos mojados");
        assertThat(acta.getAdministradorUsername()).isEqualTo("admin");
        assertThat(acta.getAdministradorNombre()).isEqualTo("Ana Administradora");
        assertThat(acta.getFecha()).isEqualTo(AHORA);
    }

    @Test
    void sinJustificacionSuficienteNoSeAplicaElAjuste() {
        assertThatThrownBy(() -> comando(100.0, null).ejecutar()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> comando(100.0, "conteo").ejecutar()).isInstanceOf(IllegalArgumentException.class);

        assertThat(viruta.getStock()).isEqualTo(120);
    }

    @Test
    void unStockContadoInvalidoNoSeAplica() {
        assertThatThrownBy(() -> comando(null, "Conteo físico de la bodega").ejecutar())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> comando(-1.0, "Conteo físico de la bodega").ejecutar())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> comando(301.0, "Conteo físico de la bodega").ejecutar())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacidad");

        assertThat(viruta.getStock()).isEqualTo(120);
    }
}
