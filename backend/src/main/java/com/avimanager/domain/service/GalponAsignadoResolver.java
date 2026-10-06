package com.avimanager.domain.service;

import com.avimanager.domain.exception.GalponNoAsignadoException;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

/**
 * Encuentra el galpon asignado a un worker y el lote que aloja. Lo comparten
 * IniciarTurnoService y ConsultarTurnosService.
 */
class GalponAsignadoResolver {

    record Asignacion(Galpon galpon, Lote lote) {
    }

    private final UsuarioRepositoryPort usuarioRepository;
    private final GalponRepositoryPort galponRepository;
    private final LoteRepositoryPort loteRepository;

    GalponAsignadoResolver(UsuarioRepositoryPort usuarioRepository, GalponRepositoryPort galponRepository,
                           LoteRepositoryPort loteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.galponRepository = galponRepository;
        this.loteRepository = loteRepository;
    }

    Asignacion resolver(String workerUsername) {
        String galponId = usuarioRepository.buscarPorUsername(workerUsername)
                .map(Usuario::getGalponAsignadoId)
                .orElse(null);
        if (galponId == null) {
            throw new GalponNoAsignadoException("Tu usuario no tiene un galpón asignado, así que no puede iniciar ni cerrar turnos.");
        }
        Galpon galpon = galponRepository.buscarPorId(galponId)
                .orElseThrow(() -> new GalponNoAsignadoException("Tu galpón asignado (" + galponId + ") no existe."));
        Lote lote = loteRepository.buscarPorGalpon(galponId)
                .orElseThrow(() -> new GalponNoAsignadoException("El " + galpon.getNombre() + " no tiene un lote alojado."));
        return new Asignacion(galpon, lote);
    }
}
