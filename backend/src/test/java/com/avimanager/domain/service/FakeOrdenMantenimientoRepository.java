package com.avimanager.domain.service;

import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.port.out.OrdenMantenimientoRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class FakeOrdenMantenimientoRepository implements OrdenMantenimientoRepositoryPort {

    final List<OrdenMantenimiento> guardadas = new ArrayList<>();

    @Override
    public OrdenMantenimiento guardar(OrdenMantenimiento orden) {
        guardadas.removeIf(o -> o.getId().equals(orden.getId()));
        guardadas.add(orden);
        return orden;
    }

    @Override
    public Optional<OrdenMantenimiento> buscarPorId(String ordenId) {
        return guardadas.stream().filter(o -> o.getId().equals(ordenId)).findFirst();
    }

    @Override
    public List<OrdenMantenimiento> listarTodas() {
        return List.copyOf(guardadas);
    }

    @Override
    public List<OrdenMantenimiento> listarPendientesPorGalpon(String galponId) {
        return guardadas.stream().filter(o -> o.getGalponId().equals(galponId) && o.estaPendiente()).toList();
    }
}
