package com.avimanager.domain.service;

import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.port.in.ConsultarInventarioUseCase;
import com.avimanager.domain.port.out.InventarioRepositoryPort;

import java.util.Comparator;
import java.util.List;

/** Implementa F-03.2: los insumos de la bodega, primero el alimento. */
public class ConsultarInventarioService implements ConsultarInventarioUseCase {

    private final InventarioRepositoryPort inventarioRepository;

    public ConsultarInventarioService(InventarioRepositoryPort inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    public List<Insumo> listarInsumos() {
        return inventarioRepository.listarTodos().stream()
                .sorted(Comparator.comparing(Insumo::getTipo).thenComparing(Insumo::getNombre))
                .toList();
    }
}
