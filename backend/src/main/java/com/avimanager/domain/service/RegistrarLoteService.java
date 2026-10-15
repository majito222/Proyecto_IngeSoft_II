package com.avimanager.domain.service;

import com.avimanager.domain.exception.GalponNoEncontradoException;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.in.RegistrarLoteCommand;
import com.avimanager.domain.port.in.RegistrarLoteUseCase;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;

import java.util.UUID;

/**
 * Implementa F-11.1 (registrar lote): busca el galpon y su lote activo, y deja
 * que LoteBuilder (patron Builder) aplique las reglas de capacidad y de lote
 * activo unico.
 */
public class RegistrarLoteService implements RegistrarLoteUseCase {

    private final GalponRepositoryPort galponRepository;
    private final LoteRepositoryPort loteRepository;

    public RegistrarLoteService(GalponRepositoryPort galponRepository, LoteRepositoryPort loteRepository) {
        this.galponRepository = galponRepository;
        this.loteRepository = loteRepository;
    }

    @Override
    public Lote registrarLote(RegistrarLoteCommand comando) {
        Galpon galpon = galponRepository.buscarPorId(comando.getGalponId())
                .orElseThrow(() -> new GalponNoEncontradoException(comando.getGalponId()));
        Lote loteActivo = loteRepository.buscarPorGalpon(galpon.getId()).orElse(null);

        Lote lote = Lote.builder()
                .id("lote-" + UUID.randomUUID())
                .alojadoEn(galpon, loteActivo)
                .poblacionInicial(comando.getPoblacionInicial())
                .edadSemanas(comando.getEdadSemanas())
                .build();
        return loteRepository.guardar(lote);
    }
}
