package com.avimanager.domain.port.out;

import com.avimanager.domain.model.ReporteDiario;

import java.util.Optional;

public interface ReporteDiarioRepositoryPort {

    ReporteDiario guardar(ReporteDiario reporteDiario);

    Optional<ReporteDiario> buscarPorId(String reporteId);
}
