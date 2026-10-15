package com.avimanager.domain.service;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.port.in.RegistrarGalponCommand;
import com.avimanager.domain.port.in.RegistrarGalponUseCase;
import com.avimanager.domain.port.out.GalponRepositoryPort;

import java.util.UUID;

/**
 * Implementa F-11.1 (registrar galpon). Las validaciones de creacion viven en
 * GalponBuilder (patron Builder).
 */
public class RegistrarGalponService implements RegistrarGalponUseCase {

    private final GalponRepositoryPort galponRepository;

    public RegistrarGalponService(GalponRepositoryPort galponRepository) {
        this.galponRepository = galponRepository;
    }

    @Override
    public Galpon registrarGalpon(RegistrarGalponCommand comando) {
        Galpon galpon = Galpon.builder()
                .id("galpon-" + UUID.randomUUID())
                .nombre(comando.getNombre())
                .capacidadMaxima(comando.getCapacidadMaxima())
                .build();
        return galponRepository.guardar(galpon);
    }
}
