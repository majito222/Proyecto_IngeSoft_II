package com.avimanager.domain.service.racion;

import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.service.FakePesajeRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * F-07 / RN-02: segunda estrategia del patron Strategy y la eleccion que hace
 * SelectorEstrategiaRacion segun el ultimo pesaje del lote.
 */
class RacionAjustadaPorPesoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 3, 10, 7, 0);
    // Semana 2: 17 g de racion y 120 g de peso estandar.
    private final Lote lote = new Lote("lote-3", "galpon-3", 600, 600, 2, EstadoLote.ACTIVO);
    private final RacionTablaEstandar tabla = new RacionTablaEstandar();

    private Pesaje pesaje(String id, double peso, LocalDateTime fecha) {
        return new Pesaje(id, lote, peso, 30, "operario3", fecha);
    }

    @Test
    void siLasAvesPesanMenosComenMas() {
        RacionAjustadaPorPeso ajustada = new RacionAjustadaPorPeso(tabla, pesaje("p", 113, AHORA));

        assertThat(ajustada.gramosPorAve(lote)).as("17 g x 120/113").isEqualTo(18.1);
        assertThat(ajustada.nombre()).isEqualTo("Ajustada por peso (+6.2%)");
    }

    @Test
    void siLasAvesPesanMasComenMenos() {
        RacionAjustadaPorPeso ajustada = new RacionAjustadaPorPeso(tabla, pesaje("p", 130, AHORA));

        assertThat(ajustada.gramosPorAve(lote)).as("17 g x 120/130").isEqualTo(15.7);
    }

    @Test
    void elAjusteNoPasaDel15Porciento() {
        assertThat(new RacionAjustadaPorPeso(tabla, pesaje("p", 60, AHORA)).nombre())
                .as("pesan la mitad, pero el ajuste se queda en +15%").isEqualTo("Ajustada por peso (+15.0%)");
        assertThat(new RacionAjustadaPorPeso(tabla, pesaje("p", 240, AHORA)).nombre())
                .as("pesan el doble, pero el ajuste se queda en -15%").isEqualTo("Ajustada por peso (-15.0%)");
    }

    @Test
    void elSelectorUsaLaTablaEstandarSiNoHayAjusteAprobado() {
        FakePesajeRepository pesajes = new FakePesajeRepository();
        SelectorEstrategiaRacion selector = new SelectorEstrategiaRacion(tabla, pesajes);

        assertThat(selector.para(lote)).as("sin pesajes").isSameAs(tabla);

        pesajes.guardar(pesaje("p1", 113, AHORA));
        assertThat(selector.para(lote)).as("pendiente de revision").isSameAs(tabla);

        Pesaje descartado = pesaje("p2", 113, AHORA.plusDays(7));
        descartado.descartarAjuste("zootecnista", AHORA.plusDays(7));
        pesajes.guardar(descartado);
        assertThat(selector.para(lote)).as("ajuste descartado").isSameAs(tabla);
    }

    @Test
    void elSelectorUsaLaRacionAjustadaSiElUltimoPesajeTieneElAjusteAprobado() {
        FakePesajeRepository pesajes = new FakePesajeRepository();
        SelectorEstrategiaRacion selector = new SelectorEstrategiaRacion(tabla, pesajes);
        Pesaje aprobado = pesaje("p1", 113, AHORA);
        aprobado.aprobarAjuste("zootecnista", AHORA);
        pesajes.guardar(aprobado);

        assertThat(selector.para(lote)).isInstanceOf(RacionAjustadaPorPeso.class);

        pesajes.guardar(pesaje("p2", 121, AHORA.plusDays(7)));
        assertThat(selector.para(lote)).as("un pesaje nuevo en rango vuelve a la tabla").isSameAs(tabla);
    }
}
