package com.avimanager.domain.service;

import com.avimanager.domain.event.DanoReportado;
import com.avimanager.domain.exception.GalponAjenoException;
import com.avimanager.domain.exception.GalponNoEncontradoException;
import com.avimanager.domain.exception.OrdenNoEncontradaException;
import com.avimanager.domain.model.EstadoGalpon;
import com.avimanager.domain.model.EstadoOrden;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.model.PrioridadOrden;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.TipoDano;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.GalponParaReporte;
import com.avimanager.domain.port.in.ReportarDanoCommand;
import com.avimanager.domain.port.in.ResumenOrden;
import com.avimanager.domain.service.mantenimiento.FabricaOrdenMantenimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-08: reportar danos, la bandeja del tecnico y el ciclo del galpon
 * ACTIVO -> EN_RIESGO_SANITARIO -> ACTIVO, con dobles de prueba y sin Spring.
 */
class MantenimientoServiceTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-03-10T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 3, 10, 10, 0);

    private FakeGalponRepository galpones;
    private FakeOrdenMantenimientoRepository ordenes;
    private FakePublicadorEventos publicador;
    private ReportarDanoService reportar;
    private AtenderOrdenService atender;
    private ConsultarOrdenesService consultar;

    @BeforeEach
    void setUp() {
        galpones = new FakeGalponRepository();
        ordenes = new FakeOrdenMantenimientoRepository();
        publicador = new FakePublicadorEventos();
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
        reportar = new ReportarDanoService(usuarios, galpones, ordenes, new FabricaOrdenMantenimiento(), publicador,
                RELOJ_FIJO);
        atender = new AtenderOrdenService(ordenes, galpones, usuarios, RELOJ_FIJO);
        consultar = new ConsultarOrdenesService(ordenes, galpones, usuarios, RELOJ_FIJO);

        galpones.guardar(new Galpon("galpon-1", "Galpón 1", 600));
        galpones.guardar(new Galpon("galpon-2", "Galpón 2", 900));
        usuarios.guardar(new Usuario("u1", "operario", "Pedro Operario", "x", Rol.OPERARIO, true, "galpon-1"));
        usuarios.guardar(new Usuario("u2", "admin", "Ana Administradora", "x", Rol.ADMINISTRADOR, true));
        usuarios.guardar(new Usuario("u3", "tecnico", "Tomás Técnico", "x", Rol.TECNICO, true));
    }

    private ResumenOrden reportarMalla(String usuario, String galponId) {
        return reportar.reportar(new ReportarDanoCommand(usuario, galponId, "ROTURA_MALLA", "Malla rota"));
    }

    private Galpon galpon(String id) {
        return galpones.buscarPorId(id).orElseThrow();
    }

    @Test
    void reportarUnDanoPoneElGalponEnRiesgoYCreaLaOrden_F081() {
        ResumenOrden resumen = reportarMalla("operario", null);

        assertThat(galpon("galpon-1").getEstado()).isEqualTo(EstadoGalpon.EN_RIESGO_SANITARIO);
        assertThat(ordenes.guardadas).hasSize(1);
        assertThat(resumen.getOrden().getEstado()).isEqualTo(EstadoOrden.ABIERTA);
        assertThat(resumen.getOrden().getPrioridad()).isEqualTo(PrioridadOrden.ALTA);
        assertThat(resumen.getOrden().getFechaLimite()).isEqualTo(AHORA.plusHours(12));
        assertThat(resumen.getMinutosRestantes()).isEqualTo(12 * 60);
        assertThat(resumen.getGalponNombre()).isEqualTo("Galpón 1");
        assertThat(resumen.getReportadoPorNombre()).isEqualTo("Pedro Operario");
        assertThat(publicador.publicados).singleElement().isInstanceOf(DanoReportado.class);
    }

    @Test
    void elOperarioSoloReportaEnSuGalpon() {
        assertThatThrownBy(() -> reportarMalla("operario", "galpon-2")).isInstanceOf(GalponAjenoException.class);
        assertThat(galpon("galpon-2").getEstado()).isEqualTo(EstadoGalpon.ACTIVO);
        assertThat(ordenes.guardadas).isEmpty();
    }

    @Test
    void elAdministradorReportaEnCualquierGalponPeroDebeIndicarlo() {
        reportarMalla("admin", "galpon-2");
        assertThat(galpon("galpon-2").getEstado()).isEqualTo(EstadoGalpon.EN_RIESGO_SANITARIO);

        assertThatThrownBy(() -> reportarMalla("admin", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reportarMalla("admin", "no-existe")).isInstanceOf(GalponNoEncontradoException.class);
    }

    @Test
    void unTipoDeDanoInvalidoSeRechaza() {
        assertThatThrownBy(() -> reportar.reportar(new ReportarDanoCommand("operario", null, "INUNDACION", "Agua")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INUNDACION");
        assertThatThrownBy(() -> reportar.reportar(new ReportarDanoCommand("operario", null, null, "Agua")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(galpon("galpon-1").getEstado()).isEqualTo(EstadoGalpon.ACTIVO);
    }

    @Test
    void elWorkerVeSoloSuGalponYElAdministradorTodos() {
        reportarMalla("operario", null);

        List<GalponParaReporte> delOperario = reportar.galponesParaReporte("operario");
        assertThat(delOperario).extracting(GalponParaReporte::getGalponId).containsExactly("galpon-1");
        assertThat(delOperario.get(0).getEstado()).isEqualTo(EstadoGalpon.EN_RIESGO_SANITARIO);
        assertThat(delOperario.get(0).getOrdenesPendientes()).hasSize(1);

        assertThat(reportar.galponesParaReporte("admin")).extracting(GalponParaReporte::getGalponId)
                .containsExactly("galpon-1", "galpon-2");
    }

    @Test
    void certificarLaReparacionDevuelveElGalponAActivo_F083() {
        String id = reportarMalla("operario", null).getOrden().getId();

        atender.iniciarReparacion(id, "tecnico");
        ResumenOrden certificada = atender.certificar(id, "tecnico", "Se cambió la malla");

        assertThat(certificada.getOrden().getEstado()).isEqualTo(EstadoOrden.CERTIFICADA);
        assertThat(certificada.getTecnicoNombre()).isEqualTo("Tomás Técnico");
        assertThat(certificada.getEstadoGalpon()).isEqualTo(EstadoGalpon.ACTIVO);
        assertThat(galpon("galpon-1").getEstado()).isEqualTo(EstadoGalpon.ACTIVO);
    }

    @Test
    void conDosDanosElGalponVuelveAActivoSoloAlCertificarElUltimo() {
        String primera = reportarMalla("operario", null).getOrden().getId();
        String segunda = reportar.reportar(new ReportarDanoCommand("operario", null, "DANO_TECHO", "Gotera"))
                .getOrden().getId();

        atender.certificar(primera, "tecnico", "Malla cambiada");
        assertThat(galpon("galpon-1").getEstado()).as("falta el techo").isEqualTo(EstadoGalpon.EN_RIESGO_SANITARIO);

        atender.certificar(segunda, "tecnico", "Techo sellado");
        assertThat(galpon("galpon-1").getEstado()).isEqualTo(EstadoGalpon.ACTIVO);
    }

    @Test
    void atenderUnaOrdenInexistenteFalla() {
        assertThatThrownBy(() -> atender.iniciarReparacion("no-existe", "tecnico"))
                .isInstanceOf(OrdenNoEncontradaException.class);
    }

    @Test
    void laBandejaMuestraPrimeroLasPendientesMasUrgentesYMarcaLasVencidas() {
        ordenes.guardar(orden("reciente", AHORA.minusHours(1)));
        ordenes.guardar(orden("vencida", AHORA.minusHours(14)));
        OrdenMantenimiento certificada = orden("certificada", AHORA.minusHours(20));
        certificada.certificar("tecnico", "Reparada", AHORA.minusHours(18));
        ordenes.guardar(certificada);

        List<ResumenOrden> bandeja = consultar.listarOrdenes();

        assertThat(bandeja).extracting(r -> r.getOrden().getId()).containsExactly("vencida", "reciente", "certificada");
        assertThat(bandeja.get(0).isVencida()).isTrue();
        assertThat(bandeja.get(0).getMinutosRestantes()).isEqualTo(-120);
        assertThat(bandeja.get(1).isVencida()).isFalse();
        assertThat(bandeja.get(1).getMinutosRestantes()).isEqualTo(11 * 60);
        assertThat(bandeja.get(2).isVencida()).isFalse();
    }

    private OrdenMantenimiento orden(String id, LocalDateTime reporte) {
        return new OrdenMantenimiento(id, "galpon-1", TipoDano.CORTINA_DANADA, "Cortina", PrioridadOrden.MEDIA,
                "operario", reporte, reporte.plusHours(12));
    }
}
