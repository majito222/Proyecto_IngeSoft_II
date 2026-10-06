package com.avimanager.domain.service;

import com.avimanager.domain.model.EstadoTurno;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.ConsultarTurnosUseCase;
import com.avimanager.domain.port.in.ResumenTurno;
import com.avimanager.domain.port.in.TurnoDelWorker;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Consultas de F-10: el turno de hoy de un worker (F-10.2) y el dashboard de
 * cierres pendientes con todos los turnos (F-10.3).
 */
public class ConsultarTurnosService implements ConsultarTurnosUseCase {

    private static final Comparator<Turno> MAS_RECIENTE_PRIMERO =
            Comparator.comparing(Turno::getFecha).thenComparing(Turno::getHoraInicio).reversed();

    private final GalponAsignadoResolver resolver;
    private final UsuarioRepositoryPort usuarioRepository;
    private final GalponRepositoryPort galponRepository;
    private final TurnoRepositoryPort turnoRepository;
    private final Clock clock;

    public ConsultarTurnosService(UsuarioRepositoryPort usuarioRepository, GalponRepositoryPort galponRepository,
                                  LoteRepositoryPort loteRepository, TurnoRepositoryPort turnoRepository, Clock clock) {
        this.resolver = new GalponAsignadoResolver(usuarioRepository, galponRepository, loteRepository);
        this.usuarioRepository = usuarioRepository;
        this.galponRepository = galponRepository;
        this.turnoRepository = turnoRepository;
        this.clock = clock;
    }

    @Override
    public TurnoDelWorker consultarTurnoDelWorker(String workerUsername) {
        GalponAsignadoResolver.Asignacion asignacion = resolver.resolver(workerUsername);
        ResumenTurno turnoDeHoy = turnoRepository
                .buscarPorLoteYFecha(asignacion.lote().getId(), LocalDate.now(clock))
                .map(this::resumir)
                .orElse(null);
        return new TurnoDelWorker(asignacion.galpon(), asignacion.lote(), turnoDeHoy);
    }

    @Override
    public List<ResumenTurno> listarTurnos(EstadoTurno estado, LocalDate fecha) {
        return turnoRepository.listarTodos().stream()
                .filter(t -> estado == null || t.getEstado() == estado)
                .filter(t -> fecha == null || t.getFecha().equals(fecha))
                .sorted(MAS_RECIENTE_PRIMERO)
                .map(this::resumir)
                .toList();
    }

    private ResumenTurno resumir(Turno turno) {
        String responsable = usuarioRepository.buscarPorUsername(turno.getWorkerUsername())
                .map(Usuario::getNombreCompleto)
                .orElse(turno.getWorkerUsername());
        String galpon = galponRepository.buscarPorId(turno.getGalponId())
                .map(Galpon::getNombre)
                .orElse(turno.getGalponId());
        return new ResumenTurno(turno, responsable, galpon);
    }
}
