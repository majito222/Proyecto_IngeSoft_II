package com.avimanager.domain.service;

import com.avimanager.domain.exception.TurnoNoEncontradoException;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.AprobarReporteCommand;
import com.avimanager.domain.port.in.AprobarReporteUseCase;
import com.avimanager.domain.port.out.TurnoRepositoryPort;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Implementa F-04.3. La regla de que estados se pueden aprobar la decide
 * EstadoTurno (patron State); un turno ABIERTO o bloqueado lanza
 * TurnoNoDisponibleException.
 */
public class AprobarReporteService implements AprobarReporteUseCase {

    private final TurnoRepositoryPort turnoRepository;
    private final Clock clock;

    public AprobarReporteService(TurnoRepositoryPort turnoRepository, Clock clock) {
        this.turnoRepository = turnoRepository;
        this.clock = clock;
    }

    @Override
    public Turno aprobarReporte(AprobarReporteCommand comando) {
        Turno turno = turnoRepository.listarTodos().stream()
                .filter(t -> t.getId().equals(comando.getTurnoId()))
                .findFirst()
                .orElseThrow(() -> new TurnoNoEncontradoException(comando.getTurnoId()));
        turno.aprobar(comando.getAdministradorUsername(), LocalDateTime.now(clock));
        return turnoRepository.guardar(turno);
    }
}
