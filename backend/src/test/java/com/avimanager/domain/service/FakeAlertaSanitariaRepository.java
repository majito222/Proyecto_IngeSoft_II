package com.avimanager.domain.service;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class FakeAlertaSanitariaRepository implements AlertaSanitariaRepositoryPort {

    final List<AlertaSanitaria> guardadas = new ArrayList<>();

    @Override
    public AlertaSanitaria guardar(AlertaSanitaria alertaSanitaria) {
        guardadas.removeIf(a -> a.getId().equals(alertaSanitaria.getId()));
        guardadas.add(alertaSanitaria);
        return alertaSanitaria;
    }

    @Override
    public Optional<AlertaSanitaria> buscarPorId(String alertaId) {
        return guardadas.stream().filter(a -> a.getId().equals(alertaId)).findFirst();
    }

    @Override
    public List<AlertaSanitaria> listarTodas() {
        return List.copyOf(guardadas);
    }
}
