package com.avimanager.domain.service.kpi;

import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.ResultadoKpi;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * F-04.4: cada estrategia de KPI (patron Strategy) con datos conocidos.
 */
class CalculadorasKpiTest {

    private static final LocalDate HOY = LocalDate.of(2026, 3, 10);

    // 1.000 aves iniciales, 980 vivas, 3 semanas de edad
    private final Lote lote = new Lote("lote-1", "galpon-1", 1000, 980, 3, EstadoLote.ACTIVO);

    private ReporteDiario reporte(LocalDate fecha, double consumoKg, int bandejas) {
        return new ReporteDiario("r-" + fecha + "-" + consumoKg, "lote-1", fecha, consumoKg, 0, null, bandejas, null);
    }

    private Pesaje pesaje(LocalDate fecha, double gramos) {
        return new Pesaje("p-" + fecha, lote, gramos, 30, "operario", fecha.atTime(7, 0));
    }

    private DatosKpiLote datos(List<ReporteDiario> reportes, List<Pesaje> pesajes) {
        return new DatosKpiLote(lote, HOY, reportes, pesajes, lote.getPoblacionActual());
    }

    @Test
    void laMortalidadAcumuladaEsLasAvesPerdidasSobreLaPoblacionInicial() {
        ResultadoKpi kpi = new MortalidadAcumuladaKpi().calcular(datos(List.of(), List.of()));

        assertThat(kpi.getValor()).isEqualTo(2.0); // 20 de 1.000
        assertThat(kpi.getUnidad()).isEqualTo("%");
    }

    @Test
    void laProduccionTotalSumaLasBandejasDelDia() {
        List<ReporteDiario> reportes = List.of(reporte(HOY, 40, 12), reporte(HOY, 35, 8),
                reporte(HOY.minusDays(1), 40, 15));

        ResultadoKpi kpi = new ProduccionTotalKpi().calcular(datos(reportes, List.of()));

        assertThat(kpi.getValor()).isEqualTo(20.0);
    }

    @Test
    void laConversionUsaElAlimentoYElPesoGanadoEntreLosDosUltimosPesajes() {
        LocalDate pesajeAnterior = HOY.minusDays(7);
        List<ReporteDiario> reportes = new ArrayList<>();
        reportes.add(reporte(pesajeAnterior, 999, 0));          // el dia del pesaje anterior no cuenta
        for (int dia = 6; dia >= 0; dia--) {
            reportes.add(reporte(HOY.minusDays(dia), 30, 0));   // 7 dias x 30 kg = 210 kg
        }
        reportes.add(reporte(HOY.plusDays(1), 999, 0));         // despues del ultimo pesaje tampoco

        // (600 g - 500 g) x 980 aves = 98 kg ganados -> 210 / 98 = 2.14
        ResultadoKpi kpi = new ConversionAlimenticiaKpi().calcular(
                datos(reportes, List.of(pesaje(pesajeAnterior, 500), pesaje(HOY, 600))));

        assertThat(kpi.getValor()).isEqualTo(2.14);
        assertThat(kpi.isEstimado()).isFalse();
    }

    @Test
    void conUnSoloPesajeElPesoAnteriorEsElEstandarDeLaSemanaPreviaYQuedaEstimado() {
        // Semana 3 pesada en 190 g; estandar de la semana 2 = 120 g.
        // (190 - 120) x 980 = 68.6 kg ganados; 7 dias x 20 kg = 140 kg -> 2.04
        List<ReporteDiario> reportes = new ArrayList<>();
        for (int dia = 6; dia >= 0; dia--) {
            reportes.add(reporte(HOY.minusDays(dia), 20, 0));
        }

        ResultadoKpi kpi = new ConversionAlimenticiaKpi().calcular(datos(reportes, List.of(pesaje(HOY, 190))));

        assertThat(kpi.getValor()).isEqualTo(2.04);
        assertThat(kpi.isEstimado()).isTrue();
    }

    @Test
    void sinPesajesLaConversionNoTieneValor() {
        ResultadoKpi kpi = new ConversionAlimenticiaKpi().calcular(datos(List.of(reporte(HOY, 40, 0)), List.of()));

        assertThat(kpi.getValor()).isNull();
        assertThat(kpi.getDetalle()).contains("pesajes");
    }

    @Test
    void sinConsumoEntrePesajesLaConversionNoTieneValor() {
        ResultadoKpi kpi = new ConversionAlimenticiaKpi().calcular(
                datos(List.of(), List.of(pesaje(HOY.minusDays(7), 500), pesaje(HOY, 600))));

        assertThat(kpi.getValor()).isNull();
    }

    @Test
    void sinGananciaDePesoLaConversionNoTieneValor() {
        ResultadoKpi kpi = new ConversionAlimenticiaKpi().calcular(datos(List.of(reporte(HOY, 40, 0)),
                List.of(pesaje(HOY.minusDays(7), 600), pesaje(HOY, 580))));

        assertThat(kpi.getValor()).isNull();
    }
}
