package com.avimanager.domain.service;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;

import java.util.ArrayList;
import java.util.List;

class FakeAlertaSanitariaRepository implements AlertaSanitariaRepositoryPort {

    final List<AlertaSanitaria> guardadas = new ArrayList<>();

    @Override
    public AlertaSanitaria guardar(AlertaSanitaria alertaSanitaria) {
        guardadas.add(alertaSanitaria);
        return alertaSanitaria;
    }
}
