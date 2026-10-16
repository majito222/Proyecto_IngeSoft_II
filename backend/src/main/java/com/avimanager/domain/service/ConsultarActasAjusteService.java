package com.avimanager.domain.service;

import com.avimanager.domain.model.ActaAjuste;
import com.avimanager.domain.port.in.ConsultarActasAjusteUseCase;
import com.avimanager.domain.port.out.ActaAjusteRepositoryPort;

import java.util.Comparator;
import java.util.List;

/** Historial de actas de ajuste (F-05), la mas reciente primero. */
public class ConsultarActasAjusteService implements ConsultarActasAjusteUseCase {

    private final ActaAjusteRepositoryPort actaAjusteRepository;

    public ConsultarActasAjusteService(ActaAjusteRepositoryPort actaAjusteRepository) {
        this.actaAjusteRepository = actaAjusteRepository;
    }

    @Override
    public List<ActaAjuste> listarActas() {
        return actaAjusteRepository.listarTodas().stream()
                .sorted(Comparator.comparing(ActaAjuste::getFecha).reversed())
                .toList();
    }
}
