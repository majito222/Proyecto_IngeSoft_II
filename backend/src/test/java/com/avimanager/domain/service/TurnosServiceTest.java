package com.avimanager.domain.service;

import com.avimanager.domain.exception.GalponNoAsignadoException;
import com.avimanager.domain.exception.TurnoYaIniciadoException;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.EstadoTurno;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.IniciarTurnoCommand;
import com.avimanager.domain.port.in.ResumenTurno;
import com.avimanager.domain.port.in.TurnoDelWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-10.2 (iniciar turno en el galpon asignado) y F-10.3 (dashboard de turnos),
 * con dobles de prueba y sin Spring.
 */
class TurnosServiceTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-03-10T11:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate HOY = LocalDate.of(2026, 3, 10);

    private FakeTurnoRepository turnoRepository;
    private IniciarTurnoService iniciarTurno;
    private ConsultarTurnosService consultarTurnos;

    @BeforeEach
    void setUp() {
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
        FakeGalponRepository galpones = new FakeGalponRepository();
        FakeLoteRepository lotes = new FakeLoteRepository();
        turnoRepository = new FakeTurnoRepository();
        iniciarTurno = new IniciarTurnoService(usuarios, galpones, lotes, turnoRepository, RELOJ_FIJO);
        consultarTurnos = new ConsultarTurnosService(usuarios, galpones, lotes, turnoRepository, RELOJ_FIJO);

        galpones.guardar(new Galpon("galpon-1", "Galpón 1", 600));
        galpones.guardar(new Galpon("galpon-2", "Galpón 2", 900));
        lotes.agregar(new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO));
        usuarios.guardar(new Usuario("u1", "operario", "Pedro Operario", "x", Rol.OPERARIO, true, "galpon-1"));
        usuarios.guardar(new Usuario("u2", "operario2", "Olga Operaria", "x", Rol.OPERARIO, true, "galpon-2"));
        usuarios.guardar(new Usuario("u3", "admin", "Ana Administradora", "x", Rol.ADMINISTRADOR, true));
    }

    @Test
    void iniciaUnTurnoAbiertoSobreElLoteDelGalponAsignado() {
        Turno turno = iniciarTurno.iniciarTurno(new IniciarTurnoCommand("operario"));

        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.ABIERTO);
        assertThat(turno.getLoteId()).isEqualTo("lote-1");
        assertThat(turno.getGalponId()).isEqualTo("galpon-1");
        assertThat(turno.getFecha()).isEqualTo(HOY);
        assertThat(turno.getWorkerUsername()).isEqualTo("operario");
    }

    @Test
    void unLoteTieneUnSoloTurnoPorDia() {
        iniciarTurno.iniciarTurno(new IniciarTurnoCommand("operario"));

        assertThatThrownBy(() -> iniciarTurno.iniciarTurno(new IniciarTurnoCommand("operario")))
                .isInstanceOf(TurnoYaIniciadoException.class);
    }

    @Test
    void noSePuedeIniciarTurnoSinGalponAsignadoOSinLote() {
        assertThatThrownBy(() -> iniciarTurno.iniciarTurno(new IniciarTurnoCommand("admin")))
                .isInstanceOf(GalponNoAsignadoException.class)
                .hasMessageContaining("no tiene un galpón asignado");
        assertThatThrownBy(() -> iniciarTurno.iniciarTurno(new IniciarTurnoCommand("operario2")))
                .isInstanceOf(GalponNoAsignadoException.class)
                .hasMessageContaining("no tiene un lote");
    }

    @Test
    void elWorkerVeSuGalponYElTurnoDeHoySoloCuandoLoInicio() {
        TurnoDelWorker antes = consultarTurnos.consultarTurnoDelWorker("operario");
        assertThat(antes.getGalpon().getNombre()).isEqualTo("Galpón 1");
        assertThat(antes.getTurnoDeHoy()).isNull();

        iniciarTurno.iniciarTurno(new IniciarTurnoCommand("operario"));

        TurnoDelWorker despues = consultarTurnos.consultarTurnoDelWorker("operario");
        assertThat(despues.getTurnoDeHoy().getTurno().getEstado()).isEqualTo(EstadoTurno.ABIERTO);
        assertThat(despues.getTurnoDeHoy().getResponsableNombre()).isEqualTo("Pedro Operario");
    }

    @Test
    void elDashboardFiltraPorEstadoYFechaYMuestraPrimeroLoMasReciente() {
        LocalDate ayer = HOY.minusDays(1);
        Turno cerradoAyer = new Turno("t-ayer", "operario", "lote-1", "galpon-1", ayer, ayer.atTime(6, 0));
        ReporteDiario reporte = new ReporteDiario("r-1", "lote-1", ayer, 45.0, 5, "jadeo", 12, null);
        reporte.registrarPorcentajeMortalidad(1.0);
        cerradoAyer.cerrar(reporte, ayer.atTime(14, 0));
        turnoRepository.guardar(cerradoAyer);
        iniciarTurno.iniciarTurno(new IniciarTurnoCommand("operario"));

        List<ResumenTurno> todos = consultarTurnos.listarTurnos(null, null);
        assertThat(todos).extracting(r -> r.getTurno().getFecha()).containsExactly(HOY, ayer);
        assertThat(todos.get(0).getGalponNombre()).isEqualTo("Galpón 1");

        assertThat(consultarTurnos.listarTurnos(EstadoTurno.ABIERTO, null))
                .extracting(r -> r.getTurno().getFecha()).containsExactly(HOY);
        assertThat(consultarTurnos.listarTurnos(null, ayer))
                .extracting(r -> r.getTurno().getEstado()).containsExactly(EstadoTurno.CERRADO);
        assertThat(consultarTurnos.listarTurnos(EstadoTurno.BLOQUEADO_ALERTA_SANITARIA, null)).isEmpty();
    }
}
