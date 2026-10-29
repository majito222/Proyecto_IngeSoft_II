package com.avimanager.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Lote.consolidarReporte es la unica forma de descontar bajas: consolidar un reporte
 * siempre descuenta sus bajas del lote (H-01) y la entidad protege su invariante de
 * poblacion (H-02). Ver docs/refactorizacion/DIAGNOSTICO_REFACTORIZACION.md.
 */
class LoteTest {

    private static final LocalDate HOY = LocalDate.of(2026, 3, 10);

    private final Lote lote = new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO);

    private static ReporteDiario reporte(String loteId, Integer bajas) {
        return new ReporteDiario("r-1", loteId, HOY, 11.0, bajas, bajas != null && bajas > 0 ? "jadeo" : null, 0, null);
    }

    @Test
    void consolidarElReporteDescuentaSusBajasDelLote_H01() {
        ReporteDiario reporte = reporte("lote-1", 5);

        lote.consolidarReporte(reporte);

        assertThat(reporte.getEstado()).isEqualTo(EstadoReporte.CONSOLIDADO);
        assertThat(lote.getPoblacionActual()).isEqualTo(495);
        assertThat(lote.getPoblacionInicial()).as("la poblacion inicial no cambia").isEqualTo(500);
    }

    @Test
    void unReporteSinBajasSeConsolidaSinCambiarLaPoblacion() {
        ReporteDiario reporte = reporte("lote-1", 0);

        lote.consolidarReporte(reporte);

        assertThat(reporte.getEstado()).isEqualTo(EstadoReporte.CONSOLIDADO);
        assertThat(lote.getPoblacionActual()).isEqualTo(500);
    }

    @Test
    void sePuedenDarDeBajaTodasLasAvesVivas() {
        lote.consolidarReporte(reporte("lote-1", 500));

        assertThat(lote.getPoblacionActual()).isZero();
    }

    @Test
    void rechazaMasBajasQueAvesVivasSinTocarNada_H02() {
        ReporteDiario reporte = reporte("lote-1", 501);

        assertThatThrownBy(() -> lote.consolidarReporte(reporte))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("500 aves vivas");
        assertThat(lote.getPoblacionActual()).isEqualTo(500);
        assertThat(reporte.getEstado()).as("el reporte sigue sin consolidar").isEqualTo(EstadoReporte.EN_PROCESO);
    }

    @Test
    void rechazaBajasNegativas_H02() {
        assertThatThrownBy(() -> lote.consolidarReporte(reporte("lote-1", -1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lote.getPoblacionActual()).isEqualTo(500);
    }

    @Test
    void rechazaUnReporteSinBajasRegistradas() {
        assertThatThrownBy(() -> lote.consolidarReporte(reporte("lote-1", null)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rechazaUnReporteDeOtroLote() {
        ReporteDiario ajeno = reporte("lote-2", 3);

        assertThatThrownBy(() -> lote.consolidarReporte(ajeno))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no es del lote lote-1");
        assertThat(lote.getPoblacionActual()).isEqualTo(500);
        assertThat(ajeno.getEstado()).isEqualTo(EstadoReporte.EN_PROCESO);
    }
}
