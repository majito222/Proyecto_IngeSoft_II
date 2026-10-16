package com.avimanager.domain.service;

import com.avimanager.domain.exception.ReporteYaConsolidadoException;
import com.avimanager.domain.exception.TurnoNoDisponibleException;
import com.avimanager.domain.exception.TurnoNoEncontradoException;
import com.avimanager.domain.exception.TurnosSinAprobarException;
import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.EstadoTurno;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.KpisLote;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteConsolidado;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.ResultadoKpi;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.AprobarReporteCommand;
import com.avimanager.domain.port.in.ConsolidarReporteCommand;
import com.avimanager.domain.service.kpi.CalculadorKpisLotes;
import com.avimanager.domain.service.kpi.ConversionAlimenticiaKpi;
import com.avimanager.domain.service.kpi.MortalidadAcumuladaKpi;
import com.avimanager.domain.service.kpi.ProduccionTotalKpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-04.4: aprobar el reporte de cada turno (F-04.3) y consolidar el dia (F-04.1)
 * con datos conocidos, incluida la regla "no consolidar con turnos sin aprobar".
 */
class ConsolidacionReporteDiarioTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-03-11T08:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate AYER = LocalDate.of(2026, 3, 10);

    private FakeTurnoRepository turnos;
    private FakeReporteDiarioRepository reportes;
    private FakeReporteConsolidadoRepository consolidados;
    private AprobarReporteService aprobar;
    private ConsolidarReporteDiarioService consolidar;
    private Turno turnoGalpon1;
    private Turno turnoGalpon2;

    @BeforeEach
    void setUp() {
        FakeLoteRepository lotes = new FakeLoteRepository();
        FakeGalponRepository galpones = new FakeGalponRepository();
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
        turnos = new FakeTurnoRepository();
        reportes = new FakeReporteDiarioRepository();
        consolidados = new FakeReporteConsolidadoRepository();

        CalculadorKpisLotes calculador = new CalculadorKpisLotes(
                List.of(new ConversionAlimenticiaKpi(), new MortalidadAcumuladaKpi(), new ProduccionTotalKpi()),
                lotes, galpones, reportes, new FakePesajeRepository());
        aprobar = new AprobarReporteService(turnos, RELOJ_FIJO);
        consolidar = new ConsolidarReporteDiarioService(turnos, consolidados, usuarios, calculador, RELOJ_FIJO);

        usuarios.guardar(new Usuario("u1", "admin", "Ana Administradora", "x", Rol.ADMINISTRADOR, true));
        galpones.guardar(new Galpon("galpon-1", "Galpón 1", 600));
        galpones.guardar(new Galpon("galpon-2", "Galpón 2", 900));
        lotes.agregar(new Lote("lote-1", "galpon-1", 500, 495, 3, EstadoLote.ACTIVO));
        lotes.agregar(new Lote("lote-2", "galpon-2", 800, 784, 5, EstadoLote.ACTIVO));

        turnoGalpon1 = turnoCerrado("t1", "operario", "lote-1", "galpon-1", 12);
        turnoGalpon2 = turnoCerrado("t2", "operario2", "lote-2", "galpon-2", 20);
    }

    private Turno turnoCerrado(String id, String worker, String loteId, String galponId, int bandejas) {
        Turno turno = new Turno(id, worker, loteId, galponId, AYER, AYER.atTime(6, 0));
        ReporteDiario reporte = reportes.guardar(
                new ReporteDiario("r-" + id, loteId, AYER, 45.0, 1, "jadeo", bandejas, null));
        reporte.consolidar();
        turno.cerrar(reporte, AYER.atTime(14, 0));
        return turnos.guardar(turno);
    }

    private ReporteConsolidado consolidarAyer() {
        return consolidar.consolidar(new ConsolidarReporteCommand(AYER, "admin"));
    }

    private ResultadoKpi kpi(KpisLote lote, String clave) {
        return lote.getKpis().stream().filter(k -> k.getClave().equals(clave)).findFirst().orElseThrow();
    }

    @Test
    void aprobarDejaElTurnoAprobadoANombreDelAdministrador() {
        Turno aprobado = aprobar.aprobarReporte(new AprobarReporteCommand("t1", "admin"));

        assertThat(aprobado.getEstado()).isEqualTo(EstadoTurno.APROBADO);
        assertThat(aprobado.getAprobadoPor()).isEqualTo("admin");
        assertThat(aprobado.getFechaAprobacion()).isEqualTo(LocalDateTime.of(2026, 3, 11, 8, 0));
    }

    @Test
    void unTurnoCerradoConAlertaTambienSeAprueba() {
        Turno turno = new Turno("t3", "operario3", "lote-1", "galpon-1", AYER, AYER.atTime(6, 0));
        ReporteDiario reporte = new ReporteDiario("r-t3", "lote-1", AYER, 45.0, 10, "enfermedad", 12, null);
        reporte.bloquearPorAlertaSanitaria(2.0);
        turno.bloquearPorAlertaSanitaria(reporte, new AlertaSanitaria("a1", "lote-1", "r-t3", AYER.atTime(14, 0), 2.0));
        turno.cerrarConAlertaAtendida(AYER.atTime(16, 0));
        turnos.guardar(turno);

        assertThat(aprobar.aprobarReporte(new AprobarReporteCommand("t3", "admin")).getEstado())
                .isEqualTo(EstadoTurno.APROBADO);
    }

    @Test
    void unTurnoAbiertoNoSePuedeAprobar() {
        turnos.guardar(new Turno("t4", "operario", "lote-1", "galpon-1", AYER.plusDays(1), AYER.plusDays(1).atTime(6, 0)));

        assertThatThrownBy(() -> aprobar.aprobarReporte(new AprobarReporteCommand("t4", "admin")))
                .isInstanceOf(TurnoNoDisponibleException.class);
        assertThatThrownBy(() -> aprobar.aprobarReporte(new AprobarReporteCommand("no-existe", "admin")))
                .isInstanceOf(TurnoNoEncontradoException.class);
    }

    @Test
    void noSeConsolidaMientrasQuedeUnTurnoDelDiaSinAprobar() {
        aprobar.aprobarReporte(new AprobarReporteCommand("t1", "admin"));

        assertThatThrownBy(this::consolidarAyer)
                .isInstanceOf(TurnosSinAprobarException.class)
                .hasMessageContaining("queda 1 turno sin aprobar");
        assertThat(consolidados.guardados).isEmpty();
        assertThat(turnoGalpon2.getEstado()).isEqualTo(EstadoTurno.CERRADO);
    }

    @Test
    void conTodosLosTurnosAprobadosConsolidaLosKpisDeCadaLote() {
        aprobar.aprobarReporte(new AprobarReporteCommand("t1", "admin"));
        aprobar.aprobarReporte(new AprobarReporteCommand("t2", "admin"));

        ReporteConsolidado reporte = consolidarAyer();

        assertThat(reporte.getFecha()).isEqualTo(AYER);
        assertThat(reporte.getConsolidadoPorNombre()).isEqualTo("Ana Administradora");
        assertThat(reporte.getTurnosAprobados()).isEqualTo(2);
        assertThat(reporte.getLotes()).extracting(KpisLote::getGalponNombre).containsExactly("Galpón 1", "Galpón 2");

        KpisLote galpon1 = reporte.getLotes().get(0);
        assertThat(kpi(galpon1, MortalidadAcumuladaKpi.CLAVE).getValor()).isEqualTo(1.0);   // 5 de 500
        assertThat(kpi(galpon1, ProduccionTotalKpi.CLAVE).getValor()).isEqualTo(12.0);
        assertThat(kpi(galpon1, ConversionAlimenticiaKpi.CLAVE).getValor()).isNull();       // sin pesajes
        assertThat(kpi(reporte.getLotes().get(1), MortalidadAcumuladaKpi.CLAVE).getValor()).isEqualTo(2.0);
        assertThat(consolidados.guardados).containsExactly(reporte);
    }

    @Test
    void unDiaSeConsolidaUnaSolaVez() {
        aprobar.aprobarReporte(new AprobarReporteCommand("t1", "admin"));
        aprobar.aprobarReporte(new AprobarReporteCommand("t2", "admin"));
        consolidarAyer();

        assertThatThrownBy(this::consolidarAyer).isInstanceOf(ReporteYaConsolidadoException.class);
    }

    @Test
    void noSeConsolidaUnaFechaFutura() {
        assertThatThrownBy(() -> consolidar.consolidar(new ConsolidarReporteCommand(AYER.plusDays(2), "admin")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
