package com.avimanager.domain.service;

import com.avimanager.domain.exception.GalponYaExisteException;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.port.in.RegistrarGalponCommand;
import com.avimanager.domain.port.in.RegistrarGalponUseCase;
import com.avimanager.domain.port.out.GalponRepositoryPort;

import java.util.List;

/**
 * Implementa F-11.1 (registrar galpon). Las validaciones de creacion viven en
 * GalponBuilder (patron Builder); este servicio agrega las que dependen de los
 * demas galpones: el nombre no se repite y el codigo es el siguiente consecutivo.
 */
public class RegistrarGalponService implements RegistrarGalponUseCase {

    private final GalponRepositoryPort galponRepository;

    public RegistrarGalponService(GalponRepositoryPort galponRepository) {
        this.galponRepository = galponRepository;
    }

    @Override
    public Galpon registrarGalpon(RegistrarGalponCommand comando) {
        List<Galpon> existentes = galponRepository.listarTodos();
        if (existentes.stream().anyMatch(g -> g.tieneElNombre(comando.getNombre()))) {
            throw new GalponYaExisteException(comando.getNombre().trim());
        }
        Galpon galpon = Galpon.builder()
                .id(CodigoConsecutivo.siguiente("galpon", existentes.stream().map(Galpon::getId).toList()))
                .nombre(comando.getNombre())
                .capacidadMaxima(comando.getCapacidadMaxima())
                .build();
        return galponRepository.guardar(galpon);
    }
}
