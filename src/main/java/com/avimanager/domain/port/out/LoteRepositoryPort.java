package com.avimanager.domain.port.out;

import com.avimanager.domain.model.Lote;

import java.util.Optional;

public interface LoteRepositoryPort {

    Optional<Lote> buscarPorId(String loteId);

    Lote guardar(Lote lote);
}
