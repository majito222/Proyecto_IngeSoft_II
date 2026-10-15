package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGalponRepository implements GalponRepositoryPort {

    private final Map<String, Galpon> almacen = new ConcurrentHashMap<>();

    @Override
    public Optional<Galpon> buscarPorId(String galponId) {
        return Optional.ofNullable(almacen.get(galponId));
    }

    @Override
    public Galpon guardar(Galpon galpon) {
        almacen.put(galpon.getId(), galpon);
        return galpon;
    }

    @Override
    public List<Galpon> listarTodos() {
        return List.copyOf(almacen.values());
    }
}
