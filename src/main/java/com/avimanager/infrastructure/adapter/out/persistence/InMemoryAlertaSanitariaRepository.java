package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAlertaSanitariaRepository implements AlertaSanitariaRepositoryPort {

    private final Map<String, AlertaSanitaria> almacen = new ConcurrentHashMap<>();

    @Override
    public AlertaSanitaria guardar(AlertaSanitaria alertaSanitaria) {
        almacen.put(alertaSanitaria.getId(), alertaSanitaria);
        return alertaSanitaria;
    }

    public Collection<AlertaSanitaria> listarTodas() {
        return almacen.values();
    }
}
