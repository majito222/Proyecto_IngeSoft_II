package com.avimanager.domain.service;

import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.in.FinalizarLoteUseCase;
import com.avimanager.domain.port.out.LoteRepositoryPort;

/**
 * Implementa F-11.1 (finalizar lote): el galpon queda libre para un lote nuevo.
 */
public class FinalizarLoteService implements FinalizarLoteUseCase {

    private final LoteRepositoryPort loteRepository;

    public FinalizarLoteService(LoteRepositoryPort loteRepository) {
        this.loteRepository = loteRepository;
    }

    @Override
    public Lote finalizarLote(String loteId) {
        Lote lote = loteRepository.buscarPorId(loteId).orElseThrow(() -> new LoteNoEncontradoException(loteId));
        lote.finalizar();
        return loteRepository.guardar(lote);
    }
}
