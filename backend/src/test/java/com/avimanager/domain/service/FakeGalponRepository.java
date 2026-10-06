package com.avimanager.domain.service;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.port.out.GalponRepositoryPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class FakeGalponRepository implements GalponRepositoryPort {

    private final Map<String, Galpon> almacen = new HashMap<>();

    @Override
    public Optional<Galpon> buscarPorId(String galponId) {
        return Optional.ofNullable(almacen.get(galponId));
    }

    @Override
    public Galpon guardar(Galpon galpon) {
        almacen.put(galpon.getId(), galpon);
        return galpon;
    }
}
