package com.avimanager.domain.service;

import com.avimanager.domain.model.EstadoPesaje;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.port.in.ConsultarPesajesUseCase;
import com.avimanager.domain.port.in.ResumenPesaje;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.PesajeRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.util.Comparator;
import java.util.List;

/**
 * Implementa F-07.2: listado de pesajes para el Zootecnista, con los pendientes
 * de revision primero y luego del mas reciente al mas antiguo.
 */
public class ConsultarPesajesService implements ConsultarPesajesUseCase {

    private final PesajeRepositoryPort pesajeRepository;
    private final ResumidorPesajes resumidor;

    public ConsultarPesajesService(PesajeRepositoryPort pesajeRepository, GalponRepositoryPort galponRepository,
                                   UsuarioRepositoryPort usuarioRepository) {
        this.pesajeRepository = pesajeRepository;
        this.resumidor = new ResumidorPesajes(galponRepository, usuarioRepository);
    }

    @Override
    public List<ResumenPesaje> listarPesajes() {
        Comparator<Pesaje> pendientesPrimero =
                Comparator.comparing((Pesaje p) -> p.getEstado() != EstadoPesaje.PENDIENTE_REVISION);
        return pesajeRepository.listarTodos().stream()
                .sorted(pendientesPrimero.thenComparing(Pesaje::getFechaHoraRegistro, Comparator.reverseOrder()))
                .map(resumidor::resumir)
                .toList();
    }
}
