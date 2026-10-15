package com.avimanager.infrastructure.config;

import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.model.PrioridadOrden;
import com.avimanager.domain.model.TipoDano;
import com.avimanager.domain.port.out.OrdenMantenimientoRepositoryPort;
import com.avimanager.domain.service.mantenimiento.FabricaOrdenMantenimiento;
import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.CifradorContrasenaPort;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.PesajeRepositoryPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;
import com.avimanager.domain.policy.PoliticaMortalidad;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Datos de ejemplo para probar la aplicacion sin un modulo de administracion de
 * galpones (F-11, Entrega 2). IDs fijos para facilitar las pruebas manuales.
 *
 *   galpon-1 / lote-1 (500 aves)  -> operario  (sin turno hoy: puede iniciarlo)
 *   galpon-2 / lote-2 (800 aves)  -> operario2 (sin turno hoy; ayer quedo bloqueado por alerta)
 *   galpon-3 / lote-3 (600 aves)  -> operario3 (turno de hoy ABIERTO: aparece pendiente en el dashboard)
 *                                    operario4 (mismo galpon: ve el turno de operario3 pero no puede cerrarlo)
 *   galpon-4 (vacio, 1000 aves)   -> sin lote ni workers: para probar el registro de lotes (F-11)
 *
 * Pesajes (F-07): el Galpon 1 se peso la semana pasada dentro de rango y el
 * Galpon 3 se peso hoy 6% por debajo del estandar, pendiente de que el
 * Zootecnista apruebe el ajuste de racion.
 *
 * Mantenimiento (F-08): el Galpon 2 tiene una rotura de malla reportada hace
 * 14 horas, con la orden ya vencida (RN-04) y el galpon EN_RIESGO_SANITARIO.
 *
 * Tambien crea un usuario de demo por cada rol (F-09). La contraseña de cada
 * uno es su nombre de usuario seguido de "123" (ej. operario / operario123).
 */
@Component
public class DatosDemoInitializer implements CommandLineRunner {

    private final GalponRepositoryPort galponRepository;
    private final LoteRepositoryPort loteRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final TurnoRepositoryPort turnoRepository;
    private final ReporteDiarioRepositoryPort reporteDiarioRepository;
    private final AlertaSanitariaRepositoryPort alertaSanitariaRepository;
    private final PesajeRepositoryPort pesajeRepository;
    private final OrdenMantenimientoRepositoryPort ordenMantenimientoRepository;
    private final CifradorContrasenaPort cifrador;
    private final Clock clock;

    public DatosDemoInitializer(GalponRepositoryPort galponRepository,
                                LoteRepositoryPort loteRepository,
                                UsuarioRepositoryPort usuarioRepository,
                                TurnoRepositoryPort turnoRepository,
                                ReporteDiarioRepositoryPort reporteDiarioRepository,
                                AlertaSanitariaRepositoryPort alertaSanitariaRepository,
                                PesajeRepositoryPort pesajeRepository,
                                OrdenMantenimientoRepositoryPort ordenMantenimientoRepository,
                                CifradorContrasenaPort cifrador,
                                Clock clock) {
        this.galponRepository = galponRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
        this.reporteDiarioRepository = reporteDiarioRepository;
        this.alertaSanitariaRepository = alertaSanitariaRepository;
        this.pesajeRepository = pesajeRepository;
        this.ordenMantenimientoRepository = ordenMantenimientoRepository;
        this.cifrador = cifrador;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        galponRepository.guardar(new Galpon("galpon-1", "Galpón 1", 600));
        galponRepository.guardar(new Galpon("galpon-2", "Galpón 2", 900));
        galponRepository.guardar(new Galpon("galpon-3", "Galpón 3", 700));
        galponRepository.guardar(new Galpon("galpon-4", "Galpón 4", 1000));
        loteRepository.guardar(new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO));
        loteRepository.guardar(new Lote("lote-2", "galpon-2", 800, 800, 5, EstadoLote.EN_OBSERVACION));
        loteRepository.guardar(new Lote("lote-3", "galpon-3", 600, 600, 2, EstadoLote.ACTIVO));

        crearUsuario("operario", "Pedro Operario", Rol.OPERARIO, "galpon-1");
        crearUsuario("operario2", "Olga Operaria", Rol.OPERARIO, "galpon-2");
        crearUsuario("operario3", "Óscar Operario", Rol.OPERARIO, "galpon-3");
        crearUsuario("operario4", "Raúl Operario", Rol.OPERARIO, "galpon-3");
        crearUsuario("admin", "Ana Administradora", Rol.ADMINISTRADOR, null);
        crearUsuario("veterinario", "Valeria Veterinaria", Rol.VETERINARIO, null);
        crearUsuario("dueno", "Diego Dueño", Rol.DUENO, null);
        crearUsuario("zootecnista", "Zoe Zootecnista", Rol.ZOOTECNISTA, null);
        crearUsuario("tecnico", "Tomás Técnico", Rol.TECNICO, null);

        LocalDate hoy = LocalDate.now(clock);
        LocalDate ayer = hoy.minusDays(1);
        turnoCerrado("demo-t1", "operario", "lote-1", "galpon-1", ayer, 500, 5);
        turnoBloqueado("demo-t2", "operario2", "lote-2", "galpon-2", ayer, 800, 16);
        turnoCerrado("demo-t3", "operario3", "lote-3", "galpon-3", ayer, 600, 3);
        turnoRepository.guardar(new Turno("demo-t4", "operario3", "lote-3", "galpon-3", hoy, hoy.atTime(6, 30)));

        // Semana 3: estandar 190 g, 186 g es -2.1% (en rango). Semana 2: estandar 120 g, 113 g es -5.8%.
        pesaje("demo-p1", "lote-1", 186, 30, "operario", hoy.minusDays(7).atTime(7, 0));
        pesaje("demo-p2", "lote-3", 113, 30, "operario3", hoy.atTime(7, 15));

        ordenVencida("demo-o1", "galpon-2", "operario2", LocalDateTime.now(clock).minusHours(14));
    }

    private void ordenVencida(String id, String galponId, String worker, LocalDateTime fechaReporte) {
        ordenMantenimientoRepository.guardar(new OrdenMantenimiento(id, galponId, TipoDano.ROTURA_MALLA,
                "Malla perimetral rota en el costado norte, entran aves silvestres", PrioridadOrden.ALTA, worker,
                fechaReporte, fechaReporte.plus(FabricaOrdenMantenimiento.PLAZO_REPARACION)));
        Galpon galpon = galponRepository.buscarPorId(galponId).orElseThrow();
        galpon.marcarEnRiesgoSanitario();
        galponRepository.guardar(galpon);
    }

    private void pesaje(String id, String loteId, double pesoPromedio, int muestra, String worker,
                        LocalDateTime fechaHora) {
        Lote lote = loteRepository.buscarPorId(loteId).orElseThrow();
        pesajeRepository.guardar(new Pesaje(id, lote, pesoPromedio, muestra, worker, fechaHora));
    }

    private void crearUsuario(String username, String nombre, Rol rol, String galponAsignadoId) {
        usuarioRepository.guardar(new Usuario("usr-" + username, username, nombre,
                cifrador.cifrar(username + "123"), rol, true, galponAsignadoId));
    }

    private void turnoCerrado(String id, String worker, String loteId, String galponId, LocalDate fecha,
                              int poblacion, int bajas) {
        Turno turno = new Turno(id, worker, loteId, galponId, fecha, fecha.atTime(6, 0));
        ReporteDiario reporte = reporte(id, loteId, fecha, poblacion, bajas);
        reporte.consolidar();
        reporteDiarioRepository.guardar(reporte);
        turno.cerrar(reporte, fecha.atTime(LocalTime.of(14, 15)));
        turnoRepository.guardar(turno);
    }

    private void turnoBloqueado(String id, String worker, String loteId, String galponId, LocalDate fecha,
                                int poblacion, int bajas) {
        Turno turno = new Turno(id, worker, loteId, galponId, fecha, fecha.atTime(6, 10));
        ReporteDiario reporte = reporte(id, loteId, fecha, poblacion, bajas);
        reporte.bloquearPorAlertaSanitaria(reporte.getPorcentajeMortalidad());
        reporteDiarioRepository.guardar(reporte);
        AlertaSanitaria alerta = new AlertaSanitaria("alerta-" + id, loteId, reporte.getId(),
                fecha.atTime(13, 40), reporte.getPorcentajeMortalidad());
        alertaSanitariaRepository.guardar(alerta);
        turno.bloquearPorAlertaSanitaria(reporte, alerta);
        turnoRepository.guardar(turno);
    }

    private ReporteDiario reporte(String turnoId, String loteId, LocalDate fecha, int poblacion, int bajas) {
        ReporteDiario reporte = new ReporteDiario("reporte-" + turnoId, loteId, fecha, 45.0, bajas,
                "jadeo", 12, null);
        reporte.registrarPorcentajeMortalidad(PoliticaMortalidad.calcularPorcentaje(bajas, poblacion));
        return reporte;
    }
}
