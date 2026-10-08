package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTurnoRepository implements TurnoRepositoryPort {

    private final Map<String, Turno> almacen = new ConcurrentHashMap<>();

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

    @Override
    public Optional<Turno> buscarPorAlertaSanitaria(String alertaId) {
        return almacen.values().stream().filter(t -> alertaId.equals(t.getAlertaSanitariaId())).findFirst();
    }
}
