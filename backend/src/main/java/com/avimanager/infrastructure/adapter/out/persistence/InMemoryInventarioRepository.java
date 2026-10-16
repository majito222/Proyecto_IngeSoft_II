package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.model.TipoInsumo;
import com.avimanager.domain.port.out.InventarioRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Adaptador de salida: la bodega de la granja en memoria (F-03). */
@Repository
public class InMemoryInventarioRepository implements InventarioRepositoryPort {

    private final Map<String, Insumo> almacen = new ConcurrentHashMap<>();

    @Override
    public Optional<Insumo> buscarPorId(String insumoId) {
        return Optional.ofNullable(almacen.get(insumoId));
    }

    @Override
    public Optional<Insumo> buscarPorTipo(TipoInsumo tipo) {
        return almacen.values().stream().filter(i -> i.getTipo() == tipo).findFirst();
    }

    @Override
    public List<Insumo> listarTodos() {
        return List.copyOf(almacen.values());
    }

    @Override
    public Insumo guardar(Insumo insumo) {
        almacen.put(insumo.getId(), insumo);
        return insumo;
    }
}
