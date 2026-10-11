package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.port.out.PesajeRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPesajeRepository implements PesajeRepositoryPort {

    private final Map<String, Pesaje> almacen = new ConcurrentHashMap<>();

    @Override
    public Pesaje guardar(Pesaje pesaje) {
        almacen.put(pesaje.getId(), pesaje);
        return pesaje;
    }

    @Override
    public Optional<Pesaje> buscarPorId(String pesajeId) {
        return Optional.ofNullable(almacen.get(pesajeId));
    }

    @Override
    public List<Pesaje> listarTodos() {
        return List.copyOf(almacen.values());
    }

    @Override
    public List<Pesaje> listarPorLote(String loteId) {
        return almacen.values().stream().filter(p -> p.getLoteId().equals(loteId)).toList();
    }

    @Override
    public Optional<Pesaje> ultimoPesajeDelLote(String loteId) {
        return listarPorLote(loteId).stream().max(Comparator.comparing(Pesaje::getFechaHoraRegistro));
    }
}
