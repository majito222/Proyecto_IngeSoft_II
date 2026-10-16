package com.avimanager.domain.service;

import com.avimanager.domain.model.ActaAjuste;
import com.avimanager.domain.port.out.ActaAjusteRepositoryPort;

import java.util.ArrayList;
import java.util.List;

class FakeActaAjusteRepository implements ActaAjusteRepositoryPort {

    final List<ActaAjuste> guardadas = new ArrayList<>();

    @Override
    public ActaAjuste guardar(ActaAjuste acta) {
        guardadas.add(acta);
        return acta;
    }

    @Override
    public List<ActaAjuste> listarTodas() {
        return List.copyOf(guardadas);
    }
}
