package com.avimanager.domain.service;

import com.avimanager.domain.exception.GalponNoEncontradoException;
import com.avimanager.domain.exception.UsuarioNoEncontradoException;
import com.avimanager.domain.exception.WorkerConTurnoAbiertoException;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.AsignarWorkerCommand;
import com.avimanager.domain.port.in.AsignarWorkerUseCase;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.util.Objects;

/**
 * Implementa F-11.1 (asignar worker a galpon). Un worker con un turno abierto no
 * cambia de galpon: el turno quedaria sobre un galpon que ya no es el suyo.
 */
public class AsignarWorkerService implements AsignarWorkerUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final GalponRepositoryPort galponRepository;
    private final TurnoRepositoryPort turnoRepository;

    public AsignarWorkerService(UsuarioRepositoryPort usuarioRepository, GalponRepositoryPort galponRepository,
                                TurnoRepositoryPort turnoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.galponRepository = galponRepository;
        this.turnoRepository = turnoRepository;
    }

    @Override
    public Usuario asignarWorker(AsignarWorkerCommand comando) {
        Usuario worker = usuarioRepository.buscarPorUsername(comando.getWorkerUsername())
                .orElseThrow(() -> new UsuarioNoEncontradoException(comando.getWorkerUsername()));
        String galponId = comando.getGalponId() == null || comando.getGalponId().isBlank()
                ? null : comando.getGalponId();
        if (galponId != null && galponRepository.buscarPorId(galponId).isEmpty()) {
            throw new GalponNoEncontradoException(galponId);
        }
        if (Objects.equals(galponId, worker.getGalponAsignadoId())) {
            return worker;
        }
        boolean tieneTurnoAbierto = turnoRepository.listarTodos().stream()
                .anyMatch(t -> t.esResponsable(worker.getUsername()) && t.estaAbierto());
        if (tieneTurnoAbierto) {
            throw new WorkerConTurnoAbiertoException(worker.getUsername());
        }
        worker.asignarGalpon(galponId);
        return usuarioRepository.guardar(worker);
    }
}
