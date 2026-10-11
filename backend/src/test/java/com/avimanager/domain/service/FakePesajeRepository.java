package com.avimanager.domain.service;

import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.port.out.PesajeRepositoryPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FakePesajeRepository implements PesajeRepositoryPort {

    final List<Pesaje> guardados = new ArrayList<>();

    @Override
    public Pesaje guardar(Pesaje pesaje) {
        guardados.removeIf(p -> p.getId().equals(pesaje.getId()));
        guardados.add(pesaje);
        return pesaje;
    }

    @Override
    public Optional<Pesaje> buscarPorId(String pesajeId) {
        return guardados.stream().filter(p -> p.getId().equals(pesajeId)).findFirst();
    }

    @Override
    public List<Pesaje> listarTodos() {
        return List.copyOf(guardados);
    }

    @Override
    public List<Pesaje> listarPorLote(String loteId) {
        return guardados.stream().filter(p -> p.getLoteId().equals(loteId)).toList();
    }

    @Override
    public Optional<Pesaje> ultimoPesajeDelLote(String loteId) {
        return listarPorLote(loteId).stream().max(Comparator.comparing(Pesaje::getFechaHoraRegistro));
    }
}
