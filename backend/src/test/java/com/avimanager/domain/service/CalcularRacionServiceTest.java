package com.avimanager.domain.service;

import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.in.RacionDiaria;
import com.avimanager.domain.service.racion.EstrategiaRacion;
import com.avimanager.domain.service.racion.RacionTablaEstandar;
import com.avimanager.domain.service.racion.SelectorEstrategiaRacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-06: racion del dia = aves vivas x gramos por ave, con la estrategia que
 * elige el selector (patron Strategy). Sin Spring.
 */
class CalcularRacionServiceTest {

    private FakeLoteRepository lotes;
    private FakeGalponRepository galpones;

    @BeforeEach
    void setUp() {
        lotes = new FakeLoteRepository();
        galpones = new FakeGalponRepository();
        galpones.guardar(new Galpon("galpon-1", "Galpón 1", 600));
        galpones.guardar(new Galpon("galpon-2", "Galpón 2", 900));
        lotes.agregar(new Lote("lote-2", "galpon-2", 800, 800, 5, EstadoLote.EN_OBSERVACION));
        lotes.agregar(new Lote("lote-1", "galpon-1", 500, 495, 3, EstadoLote.ACTIVO));
    }

    private CalcularRacionService servicioCon(EstrategiaRacion estrategia) {
        return new CalcularRacionService(lotes, galpones, new SelectorEstrategiaRacion(estrategia));
    }

    @Test
    void calculaLaRacionConLasAvesVivasYLaTablaEstandar() {
        RacionDiaria racion = servicioCon(new RacionTablaEstandar()).racionDelLote("lote-1");

        assertThat(racion.getAvesVivas()).as("usa la poblacion actual, no la inicial").isEqualTo(495);
        assertThat(racion.getGramosPorAve()).isEqualTo(23.0);
        assertThat(racion.getKilosTotales()).as("495 x 23 g = 11.385 kg, redondeado").isEqualTo(11.4);
        assertThat(racion.getGalponNombre()).isEqualTo("Galpón 1");
        assertThat(racion.getEstrategia()).isEqualTo("Tabla de nutrición estándar");
    }

    @Test
    void listaLaRacionDeTodosLosLotesOrdenadaPorGalpon() {
        List<RacionDiaria> raciones = servicioCon(new RacionTablaEstandar()).racionesDelDia();

        assertThat(raciones).extracting(RacionDiaria::getGalponId).containsExactly("galpon-1", "galpon-2");
        assertThat(raciones.get(1).getKilosTotales()).as("800 x 35 g").isEqualTo(28.0);
    }

    @Test
    void usaLaEstrategiaQueRecibeSinSaberCualEs() {
        EstrategiaRacion fija = new EstrategiaRacion() {
            @Override
            public double gramosPorAve(Lote lote) {
                return 100;
            }

            @Override
            public String nombre() {
                return "Estrategia de prueba";
            }
        };

        RacionDiaria racion = servicioCon(fija).racionDelLote("lote-2");

        assertThat(racion.getKilosTotales()).isEqualTo(80.0);
        assertThat(racion.getEstrategia()).isEqualTo("Estrategia de prueba");
    }

    @Test
    void fallaSiElLoteNoExiste() {
        assertThatThrownBy(() -> servicioCon(new RacionTablaEstandar()).racionDelLote("no-existe"))
                .isInstanceOf(LoteNoEncontradoException.class);
    }
}
