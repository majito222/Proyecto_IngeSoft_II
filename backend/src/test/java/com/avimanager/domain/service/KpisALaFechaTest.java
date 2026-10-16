package com.avimanager.domain.service;

import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.KpisLote;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteConsolidado;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.service.kpi.CalculadorKpisLotes;
import com.avimanager.domain.service.kpi.ConversionAlimenticiaKpi;
import com.avimanager.domain.service.kpi.MortalidadAcumuladaKpi;
import com.avimanager.domain.service.kpi.ProduccionTotalKpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * F-04.2: los indicadores de una fecha pasada muestran el estado de ese dia, no
 * el de hoy, y un dia consolidado es una foto que no cambia.
 */
class KpisALaFechaTest {

    private FakeLoteRepository lotes;
    private FakeReporteDiarioRepository reportes;
    private FakeReporteConsolidadoRepository consolidados;
    private CalculadorKpisLotes calculador;
    private ConsultarKpisService consultar;

    @BeforeEach
    void setUp() {
        lotes = new FakeLoteRepository();
        reportes = new FakeReporteDiarioRepository();
        consolidados = new FakeReporteConsolidadoRepository();
        FakeGalponRepository galpones = new FakeGalponRepository();
        calculador = new CalculadorKpisLotes(
                List.of(new ConversionAlimenticiaKpi(), new MortalidadAcumuladaKpi(), new ProduccionTotalKpi()),
                lotes, galpones, reportes, new FakePesajeRepository());
        consultar = new ConsultarKpisService(calculador, consolidados);

        galpones.guardar(new Galpon("galpon-1", "Galpón 1", 600));
        galpones.guardar(new Galpon("galpon-2", "Galpón 2", 900));
        // Lote de 500 aves que llego el 1 de marzo: 5 bajas el 8 y 7 bajas el 10 -> hoy tiene 488.
        lotes.agregar(new Lote("lote-1", "galpon-1", 500, 488, 3, EstadoLote.ACTIVO, LocalDate.of(2026, 3, 1)));
        cierreConsolidado("lote-1", LocalDate.of(2026, 3, 8), 5);
        cierreConsolidado("lote-1", LocalDate.of(2026, 3, 10), 7);
    }

    private void cierreConsolidado(String loteId, LocalDate fecha, int bajas) {
        ReporteDiario reporte = new ReporteDiario("r-" + loteId + "-" + fecha, loteId, fecha, 10.0, bajas,
                bajas > 0 ? "jadeo" : null, 0, null);
        reporte.consolidar();
        reportes.guardar(reporte);
    }

    private KpisLote kpisDe(LocalDate fecha, String loteId) {
        return consultar.calcularKpis(fecha).stream().filter(k -> k.getLoteId().equals(loteId)).findFirst().orElseThrow();
    }

    private double mortalidad(KpisLote lote) {
        return lote.getKpis().stream().filter(k -> k.getClave().equals(MortalidadAcumuladaKpi.CLAVE))
                .findFirst().orElseThrow().getValor();
    }

    @Test
    void lasAvesVivasYLaMortalidadSonLasDeLaFechaConsultada() {
        assertThat(kpisDe(LocalDate.of(2026, 3, 7), "lote-1").getAvesVivas()).as("antes de las bajas").isEqualTo(500);
        assertThat(mortalidad(kpisDe(LocalDate.of(2026, 3, 7), "lote-1"))).isEqualTo(0.0);

        assertThat(kpisDe(LocalDate.of(2026, 3, 8), "lote-1").getAvesVivas()).isEqualTo(495);
        assertThat(mortalidad(kpisDe(LocalDate.of(2026, 3, 8), "lote-1"))).isEqualTo(1.0);

        assertThat(kpisDe(LocalDate.of(2026, 3, 10), "lote-1").getAvesVivas()).as("hoy").isEqualTo(488);
        assertThat(mortalidad(kpisDe(LocalDate.of(2026, 3, 10), "lote-1"))).isEqualTo(2.4);
    }

    @Test
    void unLoteNoApareceEnLosDiasAntesDeSuIngreso() {
        lotes.agregar(new Lote("lote-2", "galpon-2", 800, 800, 0, EstadoLote.ACTIVO, LocalDate.of(2026, 3, 9)));

        assertThat(consultar.calcularKpis(LocalDate.of(2026, 3, 8))).extracting(KpisLote::getLoteId)
                .containsExactly("lote-1");
        assertThat(consultar.calcularKpis(LocalDate.of(2026, 3, 9))).extracting(KpisLote::getLoteId)
                .containsExactly("lote-1", "lote-2");
    }

    @Test
    void unDiaConsolidadoEsUnaFotoQueNoCambia() {
        LocalDate dia = LocalDate.of(2026, 3, 8);
        List<KpisLote> foto = calculador.calcular(dia);
        consolidados.guardar(new ReporteConsolidado("rc-1", dia, "admin", "Ana Administradora",
                LocalDateTime.of(2026, 3, 9, 8, 0), 1, foto, Map.of()));

        // Despues aparece un cierre de ese mismo dia (por ejemplo, una alerta atendida tarde).
        cierreConsolidado("lote-1", dia, 3);

        assertThat(consultar.calcularKpis(dia)).isEqualTo(foto);
        assertThat(kpisDe(dia, "lote-1").getAvesVivas()).isEqualTo(495);
    }
}
