package com.avimanager.domain.service;

import com.avimanager.domain.exception.CapacidadGalponExcedidaException;
import com.avimanager.domain.exception.GalponConLoteActivoException;
import com.avimanager.domain.exception.GalponNoEncontradoException;
import com.avimanager.domain.exception.UsuarioNoEncontradoException;
import com.avimanager.domain.exception.UsuarioNoEsWorkerException;
import com.avimanager.domain.exception.WorkerConTurnoAbiertoException;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.AsignarWorkerCommand;
import com.avimanager.domain.port.in.RegistrarGalponCommand;
import com.avimanager.domain.port.in.RegistrarLoteCommand;
import com.avimanager.domain.port.in.ResumenGalpon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-11.3: casos de uso de galpones, lotes y asignacion de workers, con dobles
 * de prueba y sin Spring.
 */
class GalponesServiceTest {

    private FakeGalponRepository galpones;
    private FakeLoteRepository lotes;
    private FakeUsuarioRepository usuarios;
    private FakeTurnoRepository turnos;
    private RegistrarGalponService registrarGalpon;
    private RegistrarLoteService registrarLote;
    private FinalizarLoteService finalizarLote;
    private AsignarWorkerService asignarWorker;
    private ConsultarGalponesService consultarGalpones;

    @BeforeEach
    void setUp() {
        galpones = new FakeGalponRepository();
        lotes = new FakeLoteRepository();
        usuarios = new FakeUsuarioRepository();
        turnos = new FakeTurnoRepository();
        registrarGalpon = new RegistrarGalponService(galpones);
        registrarLote = new RegistrarLoteService(galpones, lotes);
        finalizarLote = new FinalizarLoteService(lotes);
        asignarWorker = new AsignarWorkerService(usuarios, galpones, turnos);
        consultarGalpones = new ConsultarGalponesService(galpones, lotes, usuarios);

        galpones.guardar(new Galpon("galpon-1", "Galpón 1", 600));
        galpones.guardar(new Galpon("galpon-2", "Galpón 2", 900));
        lotes.agregar(new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO));
        usuarios.guardar(new Usuario("u1", "operario", "Pedro Operario", "x", Rol.OPERARIO, true, "galpon-1"));
        usuarios.guardar(new Usuario("u2", "operario2", "Olga Operaria", "x", Rol.OPERARIO, true));
        usuarios.guardar(new Usuario("u3", "admin", "Ana Administradora", "x", Rol.ADMINISTRADOR, true));
    }

    @Test
    void registraUnGalponNuevo() {
        Galpon galpon = registrarGalpon.registrarGalpon(new RegistrarGalponCommand("Galpón 3", 750));

        assertThat(galpones.buscarPorId(galpon.getId())).contains(galpon);
        assertThat(galpon.getCapacidadMaxima()).isEqualTo(750);
    }

    @Test
    void registraUnLoteEnUnGalponLibre() {
        Lote lote = registrarLote.registrarLote(new RegistrarLoteCommand("galpon-2", 900, 0));

        assertThat(lotes.buscarPorGalpon("galpon-2")).contains(lote);
        assertThat(lote.getPoblacionActual()).isEqualTo(900);
    }

    @Test
    void rechazaUnLoteQueSuperaLaCapacidadDelGalpon() {
        assertThatThrownBy(() -> registrarLote.registrarLote(new RegistrarLoteCommand("galpon-2", 901, 0)))
                .isInstanceOf(CapacidadGalponExcedidaException.class);
        assertThat(lotes.buscarPorGalpon("galpon-2")).isEmpty();
    }

    @Test
    void rechazaUnSegundoLoteActivoEnElMismoGalpon() {
        assertThatThrownBy(() -> registrarLote.registrarLote(new RegistrarLoteCommand("galpon-1", 100, 0)))
                .isInstanceOf(GalponConLoteActivoException.class);
        assertThat(lotes.listarTodos()).hasSize(1);
    }

    @Test
    void rechazaUnLoteEnUnGalponQueNoExiste() {
        assertThatThrownBy(() -> registrarLote.registrarLote(new RegistrarLoteCommand("galpon-9", 100, 0)))
                .isInstanceOf(GalponNoEncontradoException.class);
    }

    @Test
    void alFinalizarElLoteElGalponRecibeUnoNuevoQuePasaASerSuLoteActivo() {
        finalizarLote.finalizarLote("lote-1");
        Lote nuevo = registrarLote.registrarLote(new RegistrarLoteCommand("galpon-1", 550, 0));

        assertThat(lotes.buscarPorId("lote-1")).get().extracting(Lote::getEstado).isEqualTo(EstadoLote.FINALIZADO);
        assertThat(lotes.buscarPorGalpon("galpon-1")).contains(nuevo);
    }

    @Test
    void asignaUnWorkerAUnGalpon() {
        Usuario worker = asignarWorker.asignarWorker(new AsignarWorkerCommand("operario2", "galpon-2"));

        assertThat(worker.getGalponAsignadoId()).isEqualTo("galpon-2");
        assertThat(usuarios.buscarPorUsername("operario2")).get()
                .extracting(Usuario::getGalponAsignadoId).isEqualTo("galpon-2");
    }

    @Test
    void sinGalponElWorkerQuedaSinAsignacion() {
        Usuario worker = asignarWorker.asignarWorker(new AsignarWorkerCommand("operario", null));

        assertThat(worker.getGalponAsignadoId()).isNull();
    }

    @Test
    void soloSeAsignanOperariosAGalponesQueExisten() {
        assertThatThrownBy(() -> asignarWorker.asignarWorker(new AsignarWorkerCommand("admin", "galpon-2")))
                .isInstanceOf(UsuarioNoEsWorkerException.class);
        assertThatThrownBy(() -> asignarWorker.asignarWorker(new AsignarWorkerCommand("operario2", "galpon-9")))
                .isInstanceOf(GalponNoEncontradoException.class);
        assertThatThrownBy(() -> asignarWorker.asignarWorker(new AsignarWorkerCommand("nadie", "galpon-2")))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void unWorkerConUnTurnoAbiertoNoCambiaDeGalpon() {
        LocalDate hoy = LocalDate.of(2026, 3, 10);
        turnos.guardar(new Turno("t1", "operario", "lote-1", "galpon-1", hoy, hoy.atTime(6, 0)));

        assertThatThrownBy(() -> asignarWorker.asignarWorker(new AsignarWorkerCommand("operario", "galpon-2")))
                .isInstanceOf(WorkerConTurnoAbiertoException.class);
        assertThat(usuarios.buscarPorUsername("operario")).get()
                .extracting(Usuario::getGalponAsignadoId).isEqualTo("galpon-1");
    }

    @Test
    void listaCadaGalponConSuLoteActivoYSusWorkers() {
        List<ResumenGalpon> resumen = consultarGalpones.listarGalpones();

        assertThat(resumen).extracting(r -> r.getGalpon().getId()).containsExactly("galpon-1", "galpon-2");
        assertThat(resumen.get(0).getLoteActivo().getId()).isEqualTo("lote-1");
        assertThat(resumen.get(0).getWorkers()).extracting(Usuario::getUsername).containsExactly("operario");
        assertThat(resumen.get(1).getLoteActivo()).isNull();
        assertThat(resumen.get(1).getWorkers()).isEmpty();
    }

    @Test
    void losWorkersAsignablesSonSoloLosOperarios() {
        assertThat(consultarGalpones.listarWorkers()).extracting(Usuario::getUsername)
                .containsExactly("operario", "operario2");
    }
}
