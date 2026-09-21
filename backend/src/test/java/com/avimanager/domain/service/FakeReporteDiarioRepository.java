package com.avimanager.domain.service;

import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;

import java.util.ArrayList;
import java.util.List;

class FakeReporteDiarioRepository implements ReporteDiarioRepositoryPort {

    final List<ReporteDiario> guardados = new ArrayList<>();

    @Override
    public ReporteDiario guardar(ReporteDiario reporteDiario) {
        guardados.add(reporteDiario);
        return reporteDiario;
    }
}
