package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.Lote;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de salida: implementacion en memoria del puerto LoteRepositoryPort.
 * Se puede reemplazar por un adaptador JPA/SQL sin tocar el dominio ni los casos de uso.
 */
@Repository
public class InMemoryLoteRepository implements LoteRepositoryPort {

    private final Map<String, Lote> almacen = new ConcurrentHashMap<>();

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
