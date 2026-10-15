package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.port.out.OrdenMantenimientoRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrdenMantenimientoRepository implements OrdenMantenimientoRepositoryPort {

    private final Map<String, OrdenMantenimiento> almacen = new ConcurrentHashMap<>();

    @Override
    public OrdenMantenimiento guardar(OrdenMantenimiento orden) {
        almacen.put(orden.getId(), orden);
        return orden;
    }

    @Override
    public Optional<OrdenMantenimiento> buscarPorId(String ordenId) {
        return Optional.ofNullable(almacen.get(ordenId));
    }

    @Override
    public List<OrdenMantenimiento> listarTodas() {
        return List.copyOf(almacen.values());
    }

    @Override
    public List<OrdenMantenimiento> listarPendientesPorGalpon(String galponId) {
        return almacen.values().stream()
                .filter(o -> o.getGalponId().equals(galponId) && o.estaPendiente())
                .toList();
    }
}
