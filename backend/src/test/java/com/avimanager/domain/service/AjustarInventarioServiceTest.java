package com.avimanager.domain.service;

import com.avimanager.domain.event.StockCriticoDetectado;
import com.avimanager.domain.exception.InsumoNoEncontradoException;
import com.avimanager.domain.model.ActaAjuste;
import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.TipoInsumo;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.AjustarInventarioCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-05.3: AjustarInventarioService (RN-08), con dobles de prueba y sin Spring.
 * Es el invocador del patron Command: ejecuta el ComandoAjusteInventario y
 * guarda el acta como auditoria.
 */
class AjustarInventarioServiceTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-03-10T09:30:00Z"), ZoneOffset.UTC);
    private static final String JUSTIFICACION = "Conteo físico semanal de la bodega";

    private FakeInventarioRepository inventario;
    private FakeActaAjusteRepository actas;
    private FakePublicadorEventos publicador;
    private AjustarInventarioService service;

    @BeforeEach
    void setUp() {
        inventario = new FakeInventarioRepository();
        actas = new FakeActaAjusteRepository();
        publicador = new FakePublicadorEventos();
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
        usuarios.guardar(new Usuario("u1", "admin", "Ana Administradora", "x", Rol.ADMINISTRADOR, true));
        service = new AjustarInventarioService(inventario, actas, usuarios, publicador, RELOJ_FIJO);

        inventario.guardar(new Insumo("alimento", "Alimento balanceado", TipoInsumo.ALIMENTO, "kg", 10000, 6000));
    }

    private ActaAjuste ajustar(String insumoId, Double contado, String justificacion) {
        return service.ajustarInventario(new AjustarInventarioCommand(insumoId, contado, justificacion, "admin"));
    }

    private double stockDeAlimento() {
        return inventario.buscarPorId("alimento").orElseThrow().getStock();
    }

    @Test
    void registraElActaConLaDiferenciaYActualizaElStock() {
        ActaAjuste acta = ajustar("alimento", 5800.0, JUSTIFICACION);

        assertThat(stockDeAlimento()).isEqualTo(5800);
        assertThat(acta.getStockSistema()).isEqualTo(6000);
        assertThat(acta.getStockContado()).isEqualTo(5800);
        assertThat(acta.getDiferencia()).isEqualTo(-200);
        assertThat(acta.getJustificacion()).isEqualTo(JUSTIFICACION);
        assertThat(acta.getAdministradorUsername()).isEqualTo("admin");
        assertThat(acta.getAdministradorNombre()).isEqualTo("Ana Administradora");
        assertThat(acta.getFecha()).isEqualTo(LocalDateTime.of(2026, 3, 10, 9, 30));
        assertThat(actas.guardadas).containsExactly(acta);
    }

    @Test
    void unSobranteQuedaComoDiferenciaPositiva() {
        assertThat(ajustar("alimento", 6150.0, JUSTIFICACION).getDiferencia()).isEqualTo(150);
    }

    @Test
    void unConteoSinDiferenciaTambienQuedaRegistrado() {
        ActaAjuste acta = ajustar("alimento", 6000.0, JUSTIFICACION);

        assertThat(acta.getDiferencia()).isZero();
        assertThat(actas.guardadas).hasSize(1);
    }

    @Test
    void unAjusteInvalidoNoCambiaElStockNiDejaActa() {
        assertThatThrownBy(() -> ajustar("alimento", 5800.0, "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ajustar("alimento", 10001.0, JUSTIFICACION))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(stockDeAlimento()).isEqualTo(6000);
        assertThat(actas.guardadas).isEmpty();
    }

    @Test
    void unInsumoQueNoExisteSeRechaza() {
        assertThatThrownBy(() -> ajustar("no-existe", 10.0, JUSTIFICACION))
                .isInstanceOf(InsumoNoEncontradoException.class);
    }

    @Test
    void unAjusteQueDejaElStockBajoEl50PorCientoEmiteLaAlertaDeStockCritico() {
        ajustar("alimento", 4900.0, JUSTIFICACION);

        assertThat(publicador.publicados).singleElement().isInstanceOfSatisfying(StockCriticoDetectado.class,
                e -> assertThat(e.getStockAnterior()).isEqualTo(6000));
    }

    @Test
    void unAjusteQueNoCruzaElUmbralNoEmiteAlerta() {
        ajustar("alimento", 5000.0, JUSTIFICACION);

        assertThat(publicador.publicados).isEmpty();
    }

    @Test
    void elHistorialMuestraLaActaMasRecientePrimero() {
        Insumo alimento = inventario.buscarPorId("alimento").orElseThrow();
        actas.guardar(new ActaAjuste("vieja", alimento, 1, 1, JUSTIFICACION, "admin", "Ana",
                LocalDateTime.of(2026, 3, 1, 8, 0)));
        actas.guardar(new ActaAjuste("nueva", alimento, 1, 1, JUSTIFICACION, "admin", "Ana",
                LocalDateTime.of(2026, 3, 9, 8, 0)));

        assertThat(new ConsultarActasAjusteService(actas).listarActas())
                .extracting(ActaAjuste::getId).containsExactly("nueva", "vieja");
    }
}
