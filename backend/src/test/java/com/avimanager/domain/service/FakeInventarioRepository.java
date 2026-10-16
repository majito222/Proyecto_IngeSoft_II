package com.avimanager.domain.service;

import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.model.TipoInsumo;
import com.avimanager.domain.port.out.InventarioRepositoryPort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class FakeInventarioRepository implements InventarioRepositoryPort {

    private final Map<String, Insumo> almacen = new LinkedHashMap<>();

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
