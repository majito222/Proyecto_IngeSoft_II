package com.avimanager.domain.port.out;

import com.avimanager.domain.model.Lote;

import java.util.List;
import java.util.Optional;

public interface LoteRepositoryPort {

    Optional<Lote> buscarPorId(String loteId);

    /**
     * Lote activo que aloja hoy el galpon (un galpon tiene un lote activo a la vez).
     * Los lotes FINALIZADOS (F-11) no cuentan.
     */
    Optional<Lote> buscarPorGalpon(String galponId);

    Lote guardar(Lote lote);

    List<Lote> listarTodos();
}
