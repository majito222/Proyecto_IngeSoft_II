package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryReporteDiarioRepository implements ReporteDiarioRepositoryPort {

    private final Map<String, ReporteDiario> almacen = new ConcurrentHashMap<>();

    @Override
    public ReporteDiario guardar(ReporteDiario reporteDiario) {
        almacen.put(reporteDiario.getId(), reporteDiario);
        return reporteDiario;
    }

    @Override
    public Optional<ReporteDiario> buscarPorId(String reporteId) {
        return Optional.ofNullable(almacen.get(reporteId));
    }

    public Collection<ReporteDiario> listarTodos() {
        return almacen.values();
    }
}
