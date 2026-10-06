package com.avimanager.domain.service;

import com.avimanager.domain.exception.TurnoYaIniciadoException;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.IniciarTurnoCommand;
import com.avimanager.domain.port.in.IniciarTurnoUseCase;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementa F-10.2: el worker inicia el turno de hoy sobre el lote de su galpon
 * asignado. Un lote tiene un solo turno por dia.
 */
public class IniciarTurnoService implements IniciarTurnoUseCase {

    private final GalponAsignadoResolver resolver;
    private final TurnoRepositoryPort turnoRepository;
    private final Clock clock;

    public IniciarTurnoService(UsuarioRepositoryPort usuarioRepository, GalponRepositoryPort galponRepository,
                               LoteRepositoryPort loteRepository, TurnoRepositoryPort turnoRepository, Clock clock) {
        this.resolver = new GalponAsignadoResolver(usuarioRepository, galponRepository, loteRepository);
        this.turnoRepository = turnoRepository;
        this.clock = clock;
    }

    @Override
    public Turno iniciarTurno(IniciarTurnoCommand comando) {
        GalponAsignadoResolver.Asignacion asignacion = resolver.resolver(comando.getWorkerUsername());
        String loteId = asignacion.lote().getId();
        LocalDate hoy = LocalDate.now(clock);

        if (turnoRepository.buscarPorLoteYFecha(loteId, hoy).isPresent()) {
            throw new TurnoYaIniciadoException(loteId, hoy);
        }

        Turno turno = new Turno(UUID.randomUUID().toString(), comando.getWorkerUsername(), loteId,
                asignacion.galpon().getId(), hoy, LocalDateTime.now(clock));
        return turnoRepository.guardar(turno);
    }
}
