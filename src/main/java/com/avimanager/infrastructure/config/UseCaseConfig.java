package com.avimanager.infrastructure.config;

import com.avimanager.domain.port.in.CerrarTurnoUseCase;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.NotificadorAlertaSanitariaPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;
import com.avimanager.domain.service.CerrarTurnoService;
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

    @Bean
    public CerrarTurnoUseCase cerrarTurnoUseCase(LoteRepositoryPort loteRepository,
                                                  ReporteDiarioRepositoryPort reporteDiarioRepository,
                                                  AlertaSanitariaRepositoryPort alertaSanitariaRepository,
                                                  NotificadorAlertaSanitariaPort notificador,
                                                  Clock clock) {
        return new CerrarTurnoService(loteRepository, reporteDiarioRepository,
                alertaSanitariaRepository, notificador, clock);
    }
}
