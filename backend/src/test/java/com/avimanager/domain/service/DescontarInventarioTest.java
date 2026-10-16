package com.avimanager.domain.service;

import com.avimanager.domain.event.AlertaSanitariaGenerada;
import com.avimanager.domain.event.StockCriticoDetectado;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.TipoInsumo;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import com.avimanager.domain.service.cierre.ReglaCamposObligatorios;
import com.avimanager.domain.service.cierre.ReglaCierreTurno;
import com.avimanager.domain.service.cierre.ReglaConsolidacion;
import com.avimanager.domain.service.cierre.ReglaDescontarInventario;
import com.avimanager.domain.service.cierre.ReglaMortalidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * F-03.3: el eslabon ReglaDescontarInventario (Chain of Responsibility) dentro de
 * la cadena de cierre completa, armada igual que en UseCaseConfig, y el evento
 * StockCriticoDetectado (Observer). Bodega de 10.000 kg de alimento.
 */
class DescontarInventarioTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-03-10T14:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate HOY = LocalDate.of(2026, 3, 10);

    private FakeInventarioRepository inventario;
    private FakePublicadorEventos publicador;
    private FakeTurnoRepository turnos;
    private CerrarTurnoService service;

    @BeforeEach
    void setUp() {
        FakeLoteRepository lotes = new FakeLoteRepository();
        FakeReporteDiarioRepository reportes = new FakeReporteDiarioRepository();
        FakeAlertaSanitariaRepository alertas = new FakeAlertaSanitariaRepository();
        inventario = new FakeInventarioRepository();
        publicador = new FakePublicadorEventos();
        turnos = new FakeTurnoRepository();

        ReglaCierreTurno reglas = new ReglaCamposObligatorios();
        reglas.enlazar(new ReglaDescontarInventario(inventario, publicador))
                .enlazar(new ReglaMortalidad(lotes, reportes, alertas, turnos, publicador))
                .enlazar(new ReglaConsolidacion(lotes, reportes, turnos));
        service = new CerrarTurnoService(lotes, turnos, reglas, RELOJ_FIJO);

        lotes.agregar(new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO));
        turnos.guardar(new Turno("turno-1", "operario", "lote-1", "galpon-1", HOY, HOY.atTime(6, 0)));
    }

    private void bodegaConAlimento(double stockKg) {
        inventario.guardar(new Insumo("alimento", "Alimento balanceado", TipoInsumo.ALIMENTO, "kg", 10000, stockKg));
    }

    private CerrarTurnoResult cerrarTurno(Double consumoKg, int bajas) {
        return service.cerrarTurno(new CerrarTurnoCommand("lote-1", consumoKg, bajas, bajas > 0 ? "jadeo" : null, 12,
                null, "operario"));
    }

    private double stockDeAlimento() {
        return inventario.buscarPorTipo(TipoInsumo.ALIMENTO).orElseThrow().getStock();
    }

    private long alertasDeStock() {
        return publicador.publicados.stream().filter(e -> e instanceof StockCriticoDetectado).count();
    }

    @Test
    void unConsumoQueDejaElStockEn49PorCientoEmiteLaAlertaDeStockCritico() {
        bodegaConAlimento(5000);

        CerrarTurnoResult resultado = cerrarTurno(100.0, 1);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.TURNO_CERRADO);
        assertThat(stockDeAlimento()).isEqualTo(4900);
        assertThat(publicador.publicados).singleElement().isInstanceOfSatisfying(StockCriticoDetectado.class, e -> {
            assertThat(e.getInsumo().porcentajeDisponible()).isEqualTo(49.0);
            assertThat(e.getStockAnterior()).isEqualTo(5000);
        });
    }

    @Test
    void unConsumoQueDejaElStockEn50PorCientoNoEmiteLaAlerta() {
        bodegaConAlimento(5100);

        cerrarTurno(100.0, 1);

        assertThat(stockDeAlimento()).isEqualTo(5000);
        assertThat(alertasDeStock()).isZero();
    }

    @Test
    void siElStockYaEraCriticoNoRepiteLaAlertaEnCadaCierre() {
        bodegaConAlimento(4900);

        cerrarTurno(100.0, 1);

        assertThat(stockDeAlimento()).isEqualTo(4800);
        assertThat(alertasDeStock()).isZero();
    }

    @Test
    void elAlimentoSeDescuentaAunqueElTurnoQuedeBloqueadoPorAlertaSanitaria() {
        bodegaConAlimento(8000);

        CerrarTurnoResult resultado = cerrarTurno(45.0, 10); // 2% de mortalidad: RN-01

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.ALERTA_SANITARIA);
        assertThat(stockDeAlimento()).isEqualTo(7955);
        assertThat(publicador.publicados).singleElement().isInstanceOf(AlertaSanitariaGenerada.class);
    }

    @Test
    void conCamposIncompletosNoSeDescuentaNada() {
        bodegaConAlimento(8000);

        service.cerrarTurno(new CerrarTurnoCommand("lote-1", 45.0, 3, null, 12, null, "operario"));

        assertThat(stockDeAlimento()).isEqualTo(8000);
    }

    @Test
    void unConsumoMayorAlStockDejaLaBodegaEnCeroSinBloquearElCierre() {
        bodegaConAlimento(30);

        CerrarTurnoResult resultado = cerrarTurno(45.0, 0);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.TURNO_CERRADO);
        assertThat(stockDeAlimento()).isZero();
    }

    @Test
    void sinAlimentoRegistradoEnLaBodegaElCierreSigueNormal() {
        CerrarTurnoResult resultado = cerrarTurno(45.0, 0);

        assertThat(resultado.getEstado()).isEqualTo(CerrarTurnoResult.Estado.TURNO_CERRADO);
        assertThat(publicador.publicados).isEmpty();
    }

    @Test
    void elInventarioSeListaConElAlimentoPrimero() {
        inventario.guardar(new Insumo("viruta", "Viruta para cama", TipoInsumo.MATERIAL_CAMA, "bultos", 300, 120));
        bodegaConAlimento(5030);

        assertThat(new ConsultarInventarioService(inventario).listarInsumos())
                .extracting(Insumo::getId).containsExactly("alimento", "viruta");
    }
}
