package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAlertaSanitariaRepository implements AlertaSanitariaRepositoryPort {

    private final Map<String, AlertaSanitaria> almacen = new ConcurrentHashMap<>();

    @Override
    public AlertaSanitaria guardar(AlertaSanitaria alertaSanitaria) {
        almacen.put(alertaSanitaria.getId(), alertaSanitaria);
        return alertaSanitaria;
    }

    @Override
    public Optional<AlertaSanitaria> buscarPorId(String alertaId) {
        return Optional.ofNullable(almacen.get(alertaId));
    }

    @Override
    public List<AlertaSanitaria> listarTodas() {
        return List.copyOf(almacen.values());
    }
}
