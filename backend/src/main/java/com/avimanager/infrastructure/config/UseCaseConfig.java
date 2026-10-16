package com.avimanager.infrastructure.config;

import com.avimanager.domain.port.in.AtenderOrdenUseCase;
import com.avimanager.domain.port.in.ConsultarOrdenesUseCase;
import com.avimanager.domain.port.in.ReportarDanoUseCase;
import com.avimanager.domain.port.out.OrdenMantenimientoRepositoryPort;
import com.avimanager.domain.service.AtenderOrdenService;
import com.avimanager.domain.service.ConsultarOrdenesService;
import com.avimanager.domain.service.ReportarDanoService;
import com.avimanager.domain.service.mantenimiento.FabricaOrdenMantenimiento;
import com.avimanager.domain.port.in.AjustarInventarioUseCase;
import com.avimanager.domain.port.in.AsignarWorkerUseCase;
import com.avimanager.domain.port.in.AtenderAlertaSanitariaUseCase;
import com.avimanager.domain.port.in.CalcularRacionUseCase;
import com.avimanager.domain.port.in.CerrarTurnoUseCase;
import com.avimanager.domain.port.in.ConsultarActasAjusteUseCase;
import com.avimanager.domain.port.in.ConsultarAlertasUseCase;
import com.avimanager.domain.port.in.ConsultarGalponesUseCase;
import com.avimanager.domain.port.in.ConsultarInventarioUseCase;
import com.avimanager.domain.port.in.ConsultarPesajesUseCase;
import com.avimanager.domain.port.in.ConsultarTurnosUseCase;
import com.avimanager.domain.port.in.FinalizarLoteUseCase;
import com.avimanager.domain.port.in.IniciarSesionUseCase;
import com.avimanager.domain.port.in.IniciarTurnoUseCase;
import com.avimanager.domain.port.in.RegistrarGalponUseCase;
import com.avimanager.domain.port.in.RegistrarLoteUseCase;
import com.avimanager.domain.port.in.RegistrarPesajeUseCase;
import com.avimanager.domain.port.in.RevisarPesajeUseCase;
import com.avimanager.domain.port.out.ActaAjusteRepositoryPort;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.CifradorContrasenaPort;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.GeneradorTokenPort;
import com.avimanager.domain.port.out.InventarioRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.PesajeRepositoryPort;
import com.avimanager.domain.port.out.PublicadorEventosPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;
import com.avimanager.domain.service.AjustarInventarioService;
import com.avimanager.domain.service.AsignarWorkerService;
import com.avimanager.domain.service.AtenderAlertaSanitariaService;
import com.avimanager.domain.service.CalcularRacionService;
import com.avimanager.domain.service.CerrarTurnoService;
import com.avimanager.domain.service.ConsultarActasAjusteService;
import com.avimanager.domain.service.ConsultarAlertasService;
import com.avimanager.domain.service.ConsultarGalponesService;
import com.avimanager.domain.service.ConsultarInventarioService;
import com.avimanager.domain.service.ConsultarPesajesService;
import com.avimanager.domain.service.ConsultarTurnosService;
import com.avimanager.domain.service.FinalizarLoteService;
import com.avimanager.domain.service.IniciarSesionService;
import com.avimanager.domain.service.IniciarTurnoService;
import com.avimanager.domain.service.RegistrarGalponService;
import com.avimanager.domain.service.RegistrarLoteService;
import com.avimanager.domain.service.RegistrarPesajeService;
import com.avimanager.domain.service.RevisarPesajeService;
import com.avimanager.domain.service.cierre.ReglaCamposObligatorios;
import com.avimanager.domain.service.racion.RacionTablaEstandar;
import com.avimanager.domain.service.racion.SelectorEstrategiaRacion;
import com.avimanager.domain.service.cierre.ReglaCierreTurno;
import com.avimanager.domain.service.cierre.ReglaConsolidacion;
import com.avimanager.domain.service.cierre.ReglaDescontarInventario;
import com.avimanager.domain.service.cierre.ReglaMortalidad;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Punto unico donde Spring conecta los puertos (interfaces del dominio) con
 * el servicio de aplicacion. El dominio (CerrarTurnoService) no tiene ninguna
 * anotacion de Spring: aqui es donde se "cablea" hexagonal -> framework.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /**
     * Cadena de reglas del cierre de turno (patron Chain of Responsibility). El
     * orden importa: primero RN-05, luego el descuento de alimento de la bodega
     * (RN-03, F-03), despues RN-07/RN-01 y al final la consolidacion. El descuento
     * va antes de la mortalidad para que tambien se aplique cuando el turno queda
     * bloqueado por alerta sanitaria.
     */
    @Bean
    public ReglaCierreTurno reglasDeCierreTurno(LoteRepositoryPort loteRepository,
                                                ReporteDiarioRepositoryPort reporteDiarioRepository,
                                                AlertaSanitariaRepositoryPort alertaSanitariaRepository,
                                                TurnoRepositoryPort turnoRepository,
                                                InventarioRepositoryPort inventarioRepository,
                                                PublicadorEventosPort publicadorEventos) {
        ReglaCierreTurno primera = new ReglaCamposObligatorios();
        primera
                .enlazar(new ReglaDescontarInventario(inventarioRepository, publicadorEventos))
                .enlazar(new ReglaMortalidad(loteRepository, reporteDiarioRepository, alertaSanitariaRepository,
                        turnoRepository, publicadorEventos))
                .enlazar(new ReglaConsolidacion(loteRepository, reporteDiarioRepository, turnoRepository));
        return primera;
    }

    @Bean
    public CerrarTurnoUseCase cerrarTurnoUseCase(LoteRepositoryPort loteRepository,
                                                  TurnoRepositoryPort turnoRepository,
                                                  ReglaCierreTurno reglasDeCierreTurno,
                                                  Clock clock) {
        return new CerrarTurnoService(loteRepository, turnoRepository, reglasDeCierreTurno, clock);
    }

    @Bean
    public IniciarTurnoUseCase iniciarTurnoUseCase(UsuarioRepositoryPort usuarioRepository,
                                                    GalponRepositoryPort galponRepository,
                                                    LoteRepositoryPort loteRepository,
                                                    TurnoRepositoryPort turnoRepository,
                                                    Clock clock) {
        return new IniciarTurnoService(usuarioRepository, galponRepository, loteRepository, turnoRepository, clock);
    }

    @Bean
    public ConsultarTurnosUseCase consultarTurnosUseCase(UsuarioRepositoryPort usuarioRepository,
                                                          GalponRepositoryPort galponRepository,
                                                          LoteRepositoryPort loteRepository,
                                                          TurnoRepositoryPort turnoRepository,
                                                          Clock clock) {
        return new ConsultarTurnosService(usuarioRepository, galponRepository, loteRepository, turnoRepository, clock);
    }

    /**
     * F-06: el selector elige la estrategia de racion de cada lote (patron
     * Strategy): la tabla estandar, o la ajustada por peso si el Zootecnista
     * aprobo el ajuste del ultimo pesaje (F-07, RN-02).
     */
    @Bean
    public CalcularRacionUseCase calcularRacionUseCase(LoteRepositoryPort loteRepository,
                                                        GalponRepositoryPort galponRepository,
                                                        PesajeRepositoryPort pesajeRepository) {
        return new CalcularRacionService(loteRepository, galponRepository,
                new SelectorEstrategiaRacion(new RacionTablaEstandar(), pesajeRepository));
    }

    @Bean
    public RegistrarPesajeUseCase registrarPesajeUseCase(UsuarioRepositoryPort usuarioRepository,
                                                          GalponRepositoryPort galponRepository,
                                                          LoteRepositoryPort loteRepository,
                                                          PesajeRepositoryPort pesajeRepository,
                                                          PublicadorEventosPort publicadorEventos,
                                                          Clock clock) {
        return new RegistrarPesajeService(usuarioRepository, galponRepository, loteRepository, pesajeRepository,
                publicadorEventos, clock);
    }

    @Bean
    public RevisarPesajeUseCase revisarPesajeUseCase(PesajeRepositoryPort pesajeRepository,
                                                      GalponRepositoryPort galponRepository,
                                                      UsuarioRepositoryPort usuarioRepository,
                                                      Clock clock) {
        return new RevisarPesajeService(pesajeRepository, galponRepository, usuarioRepository, clock);
    }

    @Bean
    public ConsultarPesajesUseCase consultarPesajesUseCase(PesajeRepositoryPort pesajeRepository,
                                                            GalponRepositoryPort galponRepository,
                                                            UsuarioRepositoryPort usuarioRepository) {
        return new ConsultarPesajesService(pesajeRepository, galponRepository, usuarioRepository);
    }

    @Bean
    public ConsultarAlertasUseCase consultarAlertasUseCase(AlertaSanitariaRepositoryPort alertaSanitariaRepository,
                                                            TurnoRepositoryPort turnoRepository,
                                                            LoteRepositoryPort loteRepository,
                                                            GalponRepositoryPort galponRepository,
                                                            UsuarioRepositoryPort usuarioRepository) {
        return new ConsultarAlertasService(alertaSanitariaRepository, turnoRepository, loteRepository,
                galponRepository, usuarioRepository);
    }

    @Bean
    public AtenderAlertaSanitariaUseCase atenderAlertaSanitariaUseCase(
            AlertaSanitariaRepositoryPort alertaSanitariaRepository,
            TurnoRepositoryPort turnoRepository,
            ReporteDiarioRepositoryPort reporteDiarioRepository,
            LoteRepositoryPort loteRepository,
            GalponRepositoryPort galponRepository,
            UsuarioRepositoryPort usuarioRepository,
            PublicadorEventosPort publicadorEventos,
            Clock clock) {
        return new AtenderAlertaSanitariaService(alertaSanitariaRepository, turnoRepository, reporteDiarioRepository,
                loteRepository, galponRepository, usuarioRepository, publicadorEventos, clock);
    }

    // F-11: galpones, lotes y asignacion de workers. Galpon y Lote se crean con
    // sus Builders (patron Builder), que validan capacidad y lote activo unico.
    @Bean
    public ConsultarGalponesUseCase consultarGalponesUseCase(GalponRepositoryPort galponRepository,
                                                              LoteRepositoryPort loteRepository,
                                                              UsuarioRepositoryPort usuarioRepository) {
        return new ConsultarGalponesService(galponRepository, loteRepository, usuarioRepository);
    }

    @Bean
    public RegistrarGalponUseCase registrarGalponUseCase(GalponRepositoryPort galponRepository) {
        return new RegistrarGalponService(galponRepository);
    }

    @Bean
    public RegistrarLoteUseCase registrarLoteUseCase(GalponRepositoryPort galponRepository,
                                                      LoteRepositoryPort loteRepository) {
        return new RegistrarLoteService(galponRepository, loteRepository);
    }

    @Bean
    public FinalizarLoteUseCase finalizarLoteUseCase(LoteRepositoryPort loteRepository) {
        return new FinalizarLoteService(loteRepository);
    }

    @Bean
    public AsignarWorkerUseCase asignarWorkerUseCase(UsuarioRepositoryPort usuarioRepository,
                                                      GalponRepositoryPort galponRepository,
                                                      TurnoRepositoryPort turnoRepository) {
        return new AsignarWorkerService(usuarioRepository, galponRepository, turnoRepository);
    }

    /** F-08: la fabrica crea la orden con su prioridad y el plazo de 12 h (patron Factory, RN-04). */
    @Bean
    public ReportarDanoUseCase reportarDanoUseCase(UsuarioRepositoryPort usuarioRepository,
                                                    GalponRepositoryPort galponRepository,
                                                    OrdenMantenimientoRepositoryPort ordenMantenimientoRepository,
                                                    PublicadorEventosPort publicadorEventos,
                                                    Clock clock) {
        return new ReportarDanoService(usuarioRepository, galponRepository, ordenMantenimientoRepository,
                new FabricaOrdenMantenimiento(), publicadorEventos, clock);
    }

    @Bean
    public AtenderOrdenUseCase atenderOrdenUseCase(OrdenMantenimientoRepositoryPort ordenMantenimientoRepository,
                                                    GalponRepositoryPort galponRepository,
                                                    UsuarioRepositoryPort usuarioRepository,
                                                    Clock clock) {
        return new AtenderOrdenService(ordenMantenimientoRepository, galponRepository, usuarioRepository, clock);
    }

    @Bean
    public ConsultarOrdenesUseCase consultarOrdenesUseCase(OrdenMantenimientoRepositoryPort ordenMantenimientoRepository,
                                                            GalponRepositoryPort galponRepository,
                                                            UsuarioRepositoryPort usuarioRepository,
                                                            Clock clock) {
        return new ConsultarOrdenesService(ordenMantenimientoRepository, galponRepository, usuarioRepository, clock);
    }

    /** F-03.2: la bodega con el stock de cada insumo. */
    @Bean
    public ConsultarInventarioUseCase consultarInventarioUseCase(InventarioRepositoryPort inventarioRepository) {
        return new ConsultarInventarioService(inventarioRepository);
    }

    /**
     * F-05: Acta de Ajuste (patron Command). El servicio arma y ejecuta un
     * ComandoAjusteInventario y guarda el acta como auditoria.
     */
    @Bean
    public AjustarInventarioUseCase ajustarInventarioUseCase(InventarioRepositoryPort inventarioRepository,
                                                              ActaAjusteRepositoryPort actaAjusteRepository,
                                                              UsuarioRepositoryPort usuarioRepository,
                                                              PublicadorEventosPort publicadorEventos,
                                                              Clock clock) {
        return new AjustarInventarioService(inventarioRepository, actaAjusteRepository, usuarioRepository,
                publicadorEventos, clock);
    }

    @Bean
    public ConsultarActasAjusteUseCase consultarActasAjusteUseCase(ActaAjusteRepositoryPort actaAjusteRepository) {
        return new ConsultarActasAjusteService(actaAjusteRepository);
    }

    @Bean
    public IniciarSesionUseCase iniciarSesionUseCase(UsuarioRepositoryPort usuarioRepository,
                                                      CifradorContrasenaPort cifrador,
                                                      GeneradorTokenPort generadorToken) {
        return new IniciarSesionService(usuarioRepository, cifrador, generadorToken);
    }
}
