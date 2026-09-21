package com.avimanager.domain.service;

import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.out.LoteRepositoryPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Doble de prueba en memoria. Al no depender de Spring ni de una base de
 * datos real, el dominio puede probarse de forma aislada y rapida: esta es
 * la ventaja practica de definir los puertos en el dominio (hexagonal).
 */
class FakeLoteRepository implements LoteRepositoryPort {

    private final Map<String, Lote> almacen = new HashMap<>();

    void agregar(Lote lote) {
        almacen.put(lote.getId(), lote);
    }

    @Override
    public Optional<Lote> buscarPorId(String loteId) {
        return Optional.ofNullable(almacen.get(loteId));
    }

    @Override
    public Lote guardar(Lote lote) {
        almacen.put(lote.getId(), lote);
        return lote;
    }
}
