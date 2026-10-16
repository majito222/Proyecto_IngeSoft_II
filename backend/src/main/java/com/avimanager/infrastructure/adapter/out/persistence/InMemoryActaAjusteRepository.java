package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.ActaAjuste;
import com.avimanager.domain.port.out.ActaAjusteRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Adaptador de salida: historial de actas de ajuste en memoria (F-05). */
@Repository
public class InMemoryActaAjusteRepository implements ActaAjusteRepositoryPort {

    private final Map<String, ActaAjuste> almacen = new ConcurrentHashMap<>();

    @Override
    public ActaAjuste guardar(ActaAjuste acta) {
        almacen.put(acta.getId(), acta);
        return acta;
    }

    @Override
    public List<ActaAjuste> listarTodas() {
        return List.copyOf(almacen.values());
    }
}
