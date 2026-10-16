package com.avimanager.domain.service;

import com.avimanager.domain.exception.GalponNoEncontradoException;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.in.RegistrarLoteCommand;
import com.avimanager.domain.port.in.RegistrarLoteUseCase;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;

import java.time.Clock;
import java.time.LocalDate;


/**
 * Implementa F-11.1 (registrar lote): busca el galpon y su lote activo, y deja
 * que LoteBuilder (patron Builder) aplique las reglas de capacidad y de lote
 * activo unico.
 */
public class RegistrarLoteService implements RegistrarLoteUseCase {

    private final GalponRepositoryPort galponRepository;
    private final LoteRepositoryPort loteRepository;
    private final Clock clock;

    public RegistrarLoteService(GalponRepositoryPort galponRepository, LoteRepositoryPort loteRepository, Clock clock) {
        this.galponRepository = galponRepository;
        this.loteRepository = loteRepository;
        this.clock = clock;
    }

    @Override
    public Lote registrarLote(RegistrarLoteCommand comando) {
        Galpon galpon = galponRepository.buscarPorId(comando.getGalponId())
                .orElseThrow(() -> new GalponNoEncontradoException(comando.getGalponId()));
        Lote loteActivo = loteRepository.buscarPorGalpon(galpon.getId()).orElse(null);

        Lote lote = Lote.builder()
                .id(CodigoConsecutivo.siguiente("lote", loteRepository.listarTodos().stream().map(Lote::getId).toList()))
                .alojadoEn(galpon, loteActivo)
                .poblacionInicial(comando.getPoblacionInicial())
                .edadSemanas(comando.getEdadSemanas())
                .ingresoEl(LocalDate.now(clock))
                .build();
        return loteRepository.guardar(lote);
    }
}
