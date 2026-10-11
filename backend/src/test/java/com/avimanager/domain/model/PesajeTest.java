package com.avimanager.domain.model;

import com.avimanager.domain.exception.PesajeYaRevisadoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-07.3: el pesaje se compara con el estandar de la semana y solo se marca
 * para revision si se desvia MAS de +-5% (RN-02).
 */
class PesajeTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 3, 10, 7, 0);
    // Semana 10: peso estandar 810 g.
    private final Lote loteSemana10 = new Lote("lote-1", "galpon-1", 500, 500, 10, EstadoLote.ACTIVO);

    private Pesaje pesaje(double pesoPromedio) {
        return new Pesaje("p-1", loteSemana10, pesoPromedio, 30, "operario", AHORA);
    }

    @ParameterizedTest(name = "{0} g frente a 810 g -> {1}% -> {2}")
    @CsvSource({
            "842.4,  4.0,  DENTRO_DE_RANGO",
            "850.5,  5.0,  DENTRO_DE_RANGO",
            "858.6,  6.0,  PENDIENTE_REVISION",
            "777.6, -4.0,  DENTRO_DE_RANGO",
            "769.5, -5.0,  DENTRO_DE_RANGO",
            "761.4, -6.0,  PENDIENTE_REVISION",
    })
    void soloSeMarcaParaRevisionSiSeDesviaMasDel5Porciento(double peso, double desviacion, EstadoPesaje estado) {
        Pesaje p = pesaje(peso);

        assertThat(p.getPesoEstandarGramos()).isEqualTo(810.0);
        assertThat(p.getDesviacionPorcentaje()).isEqualTo(desviacion);
        assertThat(p.getEstado()).isEqualTo(estado);
        assertThat(p.estaFueraDeRango()).isEqualTo(estado == EstadoPesaje.PENDIENTE_REVISION);
    }

    @Test
    void guardaElLoteElGalponYLaSemanaDelPesaje() {
        Pesaje p = pesaje(800);

        assertThat(p.getLoteId()).isEqualTo("lote-1");
        assertThat(p.getGalponId()).isEqualTo("galpon-1");
        assertThat(p.getEdadSemanas()).isEqualTo(10);
        assertThat(p.getFecha()).isEqualTo(AHORA.toLocalDate());
    }

    @Test
    void elZootecnistaApruebaElAjuste() {
        Pesaje p = pesaje(700);

        p.aprobarAjuste("zootecnista", AHORA.plusHours(2));

        assertThat(p.getEstado()).isEqualTo(EstadoPesaje.AJUSTE_APROBADO);
        assertThat(p.tieneAjusteAprobado()).isTrue();
        assertThat(p.getRevisadoPor()).isEqualTo("zootecnista");
        assertThat(p.getFechaRevision()).isEqualTo(AHORA.plusHours(2));
    }

    @Test
    void elZootecnistaDescartaElAjuste() {
        Pesaje p = pesaje(700);

        p.descartarAjuste("zootecnista", AHORA.plusHours(2));

        assertThat(p.getEstado()).isEqualTo(EstadoPesaje.AJUSTE_DESCARTADO);
        assertThat(p.tieneAjusteAprobado()).isFalse();
    }

    @Test
    void noSeRevisaUnPesajeDentroDeRangoNiUnoYaRevisado() {
        assertThatThrownBy(() -> pesaje(810).aprobarAjuste("zootecnista", AHORA))
                .isInstanceOf(PesajeYaRevisadoException.class);

        Pesaje revisado = pesaje(700);
        revisado.descartarAjuste("zootecnista", AHORA);
        assertThatThrownBy(() -> revisado.aprobarAjuste("zootecnista", AHORA))
                .isInstanceOf(PesajeYaRevisadoException.class);
    }

    @Test
    void rechazaPesoOMuestraInvalidos() {
        assertThatThrownBy(() -> pesaje(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Pesaje("p", loteSemana10, 800, 0, "operario", AHORA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Pesaje("p", loteSemana10, 800, 501, "operario", AHORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aves vivas");
    }
}
