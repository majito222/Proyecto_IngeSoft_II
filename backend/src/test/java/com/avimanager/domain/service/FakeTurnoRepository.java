package com.avimanager.domain.service;

import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.out.TurnoRepositoryPort;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class FakeTurnoRepository implements TurnoRepositoryPort {

    private final Map<String, Turno> almacen = new LinkedHashMap<>();

    @Override
    public Turno guardar(Turno turno) {
        almacen.put(turno.getId(), turno);
        return turno;
    }

    @Override
    public Optional<Turno> buscarPorLoteYFecha(String loteId, LocalDate fecha) {
        return almacen.values().stream()
                .filter(t -> t.getLoteId().equals(loteId) && t.getFecha().equals(fecha))
                .findFirst();
    }

    @Override
    public List<Turno> listarTodos() {
        return List.copyOf(almacen.values());
    }
}
