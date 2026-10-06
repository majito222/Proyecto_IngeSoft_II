package com.avimanager.domain.port.out;

import com.avimanager.domain.model.Galpon;

import java.util.Optional;

public interface GalponRepositoryPort {

    Optional<Galpon> buscarPorId(String galponId);

    Galpon guardar(Galpon galpon);
}
