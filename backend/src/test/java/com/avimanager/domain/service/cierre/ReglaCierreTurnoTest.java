package com.avimanager.domain.service.cierre;

import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Mecanica de la cadena de reglas del cierre (patron Chain of Responsibility),
 * con eslabones de prueba que solo registran que se ejecutaron.
 */
class ReglaCierreTurnoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 3, 10);

    private final List<String> ejecutadas = new ArrayList<>();

    private final ContextoCierreTurno contexto = new ContextoCierreTurno(
            new CerrarTurnoCommand("lote-1", 45.0, 2, "jadeo", 12, null, "operario"),
            new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO),
            new Turno("turno-1", "operario", "lote-1", "galpon-1", HOY, HOY.atTime(6, 0)),
            LocalDateTime.of(2026, 3, 10, 14, 0));

    /** Eslabon de prueba: registra su nombre y, si se le indica, termina la cadena. */
    private ReglaCierreTurno regla(String nombre, boolean terminaLaCadena) {
        return new ReglaCierreTurno() {
            @Override
            protected Optional<CerrarTurnoResult> evaluar(ContextoCierreTurno ctx) {
                ejecutadas.add(nombre);
                return terminaLaCadena
                        ? Optional.of(CerrarTurnoResult.camposIncompletos(List.of(nombre), ctx.getTurno()))
                        : Optional.empty();
            }
        };
    }

    @Test
    void recorreLosEslabonesEnOrdenHastaQueUnoDevuelveResultado() {
        ReglaCierreTurno cadena = regla("A", false);
        cadena.enlazar(regla("B", false)).enlazar(regla("C", true)).enlazar(regla("D", true));

        CerrarTurnoResult resultado = cadena.aplicar(contexto);

        assertThat(ejecutadas).containsExactly("A", "B", "C");
        assertThat(resultado.getCamposFaltantes()).containsExactly("C");
    }

    @Test
    void unEslabonQueTerminaLaCadenaImpideQueSeEjecutenLosSiguientes() {
        ReglaCierreTurno cadena = regla("A", true);
        cadena.enlazar(regla("B", true));

        cadena.aplicar(contexto);

        assertThat(ejecutadas).containsExactly("A");
    }

    @Test
    void unaReglaNuevaSePuedeInsertarSinModificarLasDemas() {
        // Asi se agregara, por ejemplo, el descuento de inventario de F-03.
        ReglaCierreTurno cadena = regla("camposObligatorios", false);
        cadena.enlazar(regla("mortalidad", false))
                .enlazar(regla("reglaNueva", false))
                .enlazar(regla("consolidacion", true));

        cadena.aplicar(contexto);

        assertThat(ejecutadas).containsExactly("camposObligatorios", "mortalidad", "reglaNueva", "consolidacion");
    }

    @Test
    void unaCadenaQueTerminaSinResultadoEsUnErrorDeConfiguracion() {
        ReglaCierreTurno cadena = regla("A", false);

        assertThatThrownBy(() -> cadena.aplicar(contexto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ReglaConsolidacion");
    }

    @Test
    void laConsolidacionSiempreEsElUltimoEslabon() {
        ReglaConsolidacion consolidacion = new ReglaConsolidacion(null, null, null);

        assertThatThrownBy(() -> consolidacion.enlazar(regla("despues", true)))
                .isInstanceOf(IllegalStateException.class);
    }
}
