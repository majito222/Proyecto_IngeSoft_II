package com.avimanager.infrastructure.config;

import com.avimanager.domain.port.in.AtenderAlertaSanitariaUseCase;
import com.avimanager.domain.port.in.CerrarTurnoUseCase;
import com.avimanager.domain.port.in.ConsultarAlertasUseCase;
import com.avimanager.domain.port.in.ConsultarTurnosUseCase;
import com.avimanager.domain.port.in.IniciarSesionUseCase;
import com.avimanager.domain.port.in.IniciarTurnoUseCase;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.CifradorContrasenaPort;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.GeneradorTokenPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.PublicadorEventosPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;
import com.avimanager.domain.service.AtenderAlertaSanitariaService;
import com.avimanager.domain.service.CerrarTurnoService;
import com.avimanager.domain.service.ConsultarAlertasService;
import com.avimanager.domain.service.ConsultarTurnosService;
import com.avimanager.domain.service.IniciarSesionService;
import com.avimanager.domain.service.IniciarTurnoService;
import com.avimanager.domain.service.cierre.ReglaCamposObligatorios;
import com.avimanager.domain.service.cierre.ReglaCierreTurno;
import com.avimanager.domain.service.cierre.ReglaConsolidacion;
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
     * orden importa: primero RN-05, luego RN-07/RN-01 y al final la consolidacion.
     * Para agregar una regla (por ejemplo, el descuento de inventario de F-03),
     * se enlaza un eslabon nuevo antes de ReglaConsolidacion.
     */
    @Bean
    public ReglaCierreTurno reglasDeCierreTurno(LoteRepositoryPort loteRepository,
                                                ReporteDiarioRepositoryPort reporteDiarioRepository,
                                                AlertaSanitariaRepositoryPort alertaSanitariaRepository,
                                                TurnoRepositoryPort turnoRepository,
                                                PublicadorEventosPort publicadorEventos) {
        ReglaCierreTurno primera = new ReglaCamposObligatorios();
        primera
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

    @Bean
    public IniciarSesionUseCase iniciarSesionUseCase(UsuarioRepositoryPort usuarioRepository,
                                                      CifradorContrasenaPort cifrador,
                                                      GeneradorTokenPort generadorToken) {
        return new IniciarSesionService(usuarioRepository, cifrador, generadorToken);
    }
}
