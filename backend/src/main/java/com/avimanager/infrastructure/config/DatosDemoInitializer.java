package com.avimanager.infrastructure.config;

import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.model.PrioridadOrden;
import com.avimanager.domain.model.TipoDano;
import com.avimanager.domain.port.out.OrdenMantenimientoRepositoryPort;
import com.avimanager.domain.service.mantenimiento.FabricaOrdenMantenimiento;
import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.EstadoLote;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Rol;
import com.avimanager.domain.model.TipoInsumo;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.CifradorContrasenaPort;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.InventarioRepositoryPort;
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
 * Historial (F-04): las dos semanas anteriores tienen un cierre aprobado por
 * dia en cada galpon, con el consumo de su racion, y cada lote tiene dos
 * pesajes semanales (cada uno con la edad que tenia el lote ese dia). Asi la
 * conversion alimenticia muestra valores reales (alrededor de 1.6 a 2.2).
 * La produccion de huevos de la demo es 0: los lotes tienen de 2 a 5 semanas y
 * las ponedoras empiezan a poner hacia la semana 18-20.
 *
 * Pesajes (F-07): el Galpon 1 no se ha pesado esta semana (el operario puede
 * hacerlo en la demo) y el Galpon 3 se peso hoy 6% por debajo del estandar,
 * pendiente de que el Zootecnista apruebe el ajuste de racion.
 *
 * Mantenimiento (F-08): el Galpon 2 tiene una rotura de malla reportada hace
 * 14 horas, con la orden ya vencida (RN-04) y el galpon EN_RIESGO_SANITARIO.
 *
 * Bodega (F-03): el alimento esta en 50.05% de la capacidad, asi que cualquier
 * cierre de turno con mas de 5 kg lo deja bajo el 50% y emite la alerta de
 * Stock Critico.
 * La viruta para cama ya esta en Stock Critico (40%).
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
    private final InventarioRepositoryPort inventarioRepository;
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
                                InventarioRepositoryPort inventarioRepository,
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
        this.inventarioRepository = inventarioRepository;
        this.cifrador = cifrador;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        galponRepository.guardar(new Galpon("galpon-1", "Galpón 1", 600));
        galponRepository.guardar(new Galpon("galpon-2", "Galpón 2", 900));
        galponRepository.guardar(new Galpon("galpon-3", "Galpón 3", 700));
        galponRepository.guardar(new Galpon("galpon-4", "Galpón 4", 1000));
        // Cada lote ingreso al galpon hace tantas semanas como su edad (llego con 0 semanas).
        LocalDate hoyIngreso = LocalDate.now(clock);
        loteRepository.guardar(new Lote("lote-1", "galpon-1", 500, 500, 3, EstadoLote.ACTIVO, hoyIngreso.minusWeeks(3)));
        loteRepository.guardar(new Lote("lote-2", "galpon-2", 800, 800, 5, EstadoLote.EN_OBSERVACION,
                hoyIngreso.minusWeeks(5)));
        loteRepository.guardar(new Lote("lote-3", "galpon-3", 600, 600, 2, EstadoLote.ACTIVO, hoyIngreso.minusWeeks(2)));

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

        // Dos semanas de cierres aprobados (hace 13 dias hasta antier) con el consumo de cada racion.
        historial("lote-1", "galpon-1", "operario", hoy, 7.2);
        historial("lote-2", "galpon-2", "operario2", hoy, 18.2);
        historial("lote-3", "galpon-3", "operario3", hoy, 6.2);

        turnoCerrado("demo-t1", "operario", "lote-1", "galpon-1", ayer, 500, 5, 7.4);
        turnoBloqueado("demo-t2", "operario2", "lote-2", "galpon-2", ayer, 800, 16, 18.0);
        turnoCerrado("demo-t3", "operario3", "lote-3", "galpon-3", ayer, 600, 3, 6.5);
        turnoRepository.guardar(new Turno("demo-t4", "operario3", "lote-3", "galpon-3", hoy, hoy.atTime(6, 30)));

        // Cada pesaje con la edad que tenia el lote ese dia (estandar: semana 1 = 70 g, 2 = 120 g,
        // 3 = 190 g, 4 = 270 g). Todos en rango, salvo el de hoy del Galpon 3: 113 g frente a 120 g es -5.8%.
        pesaje("demo-p1a", "lote-1", 2, 71, 30, "operario", hoy.minusDays(14).atTime(7, 0));
        pesaje("demo-p1", "lote-1", 1, 118, 30, "operario", hoy.minusDays(7).atTime(7, 0));
        pesaje("demo-p3a", "lote-2", 2, 188, 30, "operario2", hoy.minusDays(14).atTime(7, 10));
        pesaje("demo-p3", "lote-2", 1, 266, 30, "operario2", hoy.minusDays(7).atTime(7, 10));
        pesaje("demo-p2a", "lote-3", 1, 69, 30, "operario3", hoy.minusDays(7).atTime(7, 15));
        pesaje("demo-p2", "lote-3", 0, 113, 30, "operario3", hoy.atTime(7, 15));

        ordenVencida("demo-o1", "galpon-2", "operario2", LocalDateTime.now(clock).minusHours(14));

        inventarioRepository.guardar(new Insumo("insumo-alimento", "Alimento balanceado", TipoInsumo.ALIMENTO,
                "kg", 10000, 5005));
        inventarioRepository.guardar(new Insumo("insumo-vacuna", "Vacuna Newcastle", TipoInsumo.SANIDAD,
                "dosis", 2000, 1500));
        inventarioRepository.guardar(new Insumo("insumo-viruta", "Viruta para cama", TipoInsumo.MATERIAL_CAMA,
                "bultos", 300, 120));
    }

    private void ordenVencida(String id, String galponId, String worker, LocalDateTime fechaReporte) {
        ordenMantenimientoRepository.guardar(new OrdenMantenimiento(id, galponId, TipoDano.ROTURA_MALLA,
                "Malla perimetral rota en el costado norte, entran aves silvestres", PrioridadOrden.ALTA, worker,
                fechaReporte, fechaReporte.plus(FabricaOrdenMantenimiento.PLAZO_REPARACION)));
        Galpon galpon = galponRepository.buscarPorId(galponId).orElseThrow();
        galpon.marcarEnRiesgoSanitario();
        galponRepository.guardar(galpon);
    }

    /**
     * Pesaje de hace {@code semanasAtras} semanas. El lote guarda solo su edad actual,
     * asi que el pesaje se registra sobre una copia con la edad que tenia ese dia.
     */
    private void pesaje(String id, String loteId, int semanasAtras, double pesoPromedio, int muestra, String worker,
                        LocalDateTime fechaHora) {
        Lote lote = loteRepository.buscarPorId(loteId).orElseThrow();
        Lote ese = new Lote(lote.getId(), lote.getGalponId(), lote.getPoblacionInicial(), lote.getPoblacionActual(),
                lote.getEdadSemanas() - semanasAtras, lote.getEstado(), lote.getFechaIngreso());
        pesajeRepository.guardar(new Pesaje(id, ese, pesoPromedio, muestra, worker, fechaHora));
    }

    /** Un cierre aprobado por dia, de hace 13 dias hasta antier, sin bajas y con el consumo de la racion. */
    private void historial(String loteId, String galponId, String worker, LocalDate hoy, double consumoKg) {
        Lote lote = loteRepository.buscarPorId(loteId).orElseThrow();
        for (int dias = 13; dias >= 2; dias--) {
            LocalDate fecha = hoy.minusDays(dias);
            String id = "hist-" + loteId + "-" + fecha;
            Turno turno = new Turno(id, worker, loteId, galponId, fecha, fecha.atTime(6, 0));
            double consumo = Math.round((consumoKg + (dias % 3) * 0.3) * 10.0) / 10.0;
            ReporteDiario reporte = new ReporteDiario("reporte-" + id, loteId, fecha, consumo, 0, null, 0,
                    "Sin novedades");
            reporte.registrarPorcentajeMortalidad(0.0);
            lote.consolidarReporte(reporte);
            reporteDiarioRepository.guardar(reporte);
            turno.cerrar(reporte, fecha.atTime(14, 0));
            turno.aprobar("admin", fecha.atTime(18, 0));
            turnoRepository.guardar(turno);
        }
        loteRepository.guardar(lote);
    }

    private void crearUsuario(String username, String nombre, Rol rol, String galponAsignadoId) {
        usuarioRepository.guardar(new Usuario("usr-" + username, username, nombre,
                cifrador.cifrar(username + "123"), rol, true, galponAsignadoId));
    }

    private void turnoCerrado(String id, String worker, String loteId, String galponId, LocalDate fecha,
                              int poblacion, int bajas, double consumoKg) {
        Turno turno = new Turno(id, worker, loteId, galponId, fecha, fecha.atTime(6, 0));
        ReporteDiario reporte = reporte(id, loteId, fecha, poblacion, bajas, consumoKg);
        // Igual que ReglaConsolidacion: consolidar el reporte descuenta sus bajas del lote.
        Lote lote = loteRepository.buscarPorId(loteId).orElseThrow();
        lote.consolidarReporte(reporte);
        loteRepository.guardar(lote);
        reporteDiarioRepository.guardar(reporte);
        turno.cerrar(reporte, fecha.atTime(LocalTime.of(14, 15)));
        turnoRepository.guardar(turno);
    }

    private void turnoBloqueado(String id, String worker, String loteId, String galponId, LocalDate fecha,
                                int poblacion, int bajas, double consumoKg) {
        Turno turno = new Turno(id, worker, loteId, galponId, fecha, fecha.atTime(6, 10));
        ReporteDiario reporte = reporte(id, loteId, fecha, poblacion, bajas, consumoKg);
        reporte.bloquearPorAlertaSanitaria(reporte.getPorcentajeMortalidad());
        reporteDiarioRepository.guardar(reporte);
        AlertaSanitaria alerta = new AlertaSanitaria("alerta-" + id, loteId, reporte.getId(),
                fecha.atTime(13, 40), reporte.getPorcentajeMortalidad());
        alertaSanitariaRepository.guardar(alerta);
        turno.bloquearPorAlertaSanitaria(reporte, alerta);
        turnoRepository.guardar(turno);
    }

    private ReporteDiario reporte(String turnoId, String loteId, LocalDate fecha, int poblacion, int bajas,
                                  double consumoKg) {
        ReporteDiario reporte = new ReporteDiario("reporte-" + turnoId, loteId, fecha, consumoKg, bajas,
                "jadeo", 0, null);
        reporte.registrarPorcentajeMortalidad(PoliticaMortalidad.calcularPorcentaje(bajas, poblacion));
        return reporte;
    }
}
