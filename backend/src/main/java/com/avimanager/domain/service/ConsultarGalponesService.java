package com.avimanager.domain.service;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.ConsultarGalponesUseCase;
import com.avimanager.domain.port.in.ResumenGalpon;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Consultas de F-11: cada galpon con su lote activo y sus workers asignados, y
 * los workers disponibles para asignar.
 */
public class ConsultarGalponesService implements ConsultarGalponesUseCase {

    private final GalponRepositoryPort galponRepository;
    private final LoteRepositoryPort loteRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public ConsultarGalponesService(GalponRepositoryPort galponRepository, LoteRepositoryPort loteRepository,
                                    UsuarioRepositoryPort usuarioRepository) {
        this.galponRepository = galponRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<ResumenGalpon> listarGalpones() {
        List<Usuario> workers = listarWorkers();
        return galponRepository.listarTodos().stream()
                .sorted(Comparator.comparing(Galpon::getNombre))
                .map(galpon -> new ResumenGalpon(
                        galpon,
                        loteRepository.buscarPorGalpon(galpon.getId()).orElse(null),
                        workers.stream()
                                .filter(w -> Objects.equals(w.getGalponAsignadoId(), galpon.getId()))
                                .toList()))
                .toList();
    }

    @Override
    public List<Usuario> listarWorkers() {
        return usuarioRepository.listarTodos().stream()
                .filter(Usuario::esWorker)
                .sorted(Comparator.comparing(Usuario::getUsername))
                .toList();
    }
}
