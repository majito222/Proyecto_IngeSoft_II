package com.avimanager.domain.service;

import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class FakeReporteDiarioRepository implements ReporteDiarioRepositoryPort {

    final List<ReporteDiario> guardados = new ArrayList<>();

    @Override
    public ReporteDiario guardar(ReporteDiario reporteDiario) {
        guardados.removeIf(r -> r.getId().equals(reporteDiario.getId()));
        guardados.add(reporteDiario);
        return reporteDiario;
    }

    @Override
    public Optional<ReporteDiario> buscarPorId(String reporteId) {
        return guardados.stream().filter(r -> r.getId().equals(reporteId)).findFirst();
    }
}
