package com.avimanager.domain.service;

import com.avimanager.domain.model.ReporteConsolidado;
import com.avimanager.domain.port.out.ReporteConsolidadoRepositoryPort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class FakeReporteConsolidadoRepository implements ReporteConsolidadoRepositoryPort {

    final List<ReporteConsolidado> guardados = new ArrayList<>();

    @Override
    public ReporteConsolidado guardar(ReporteConsolidado reporte) {
        guardados.add(reporte);
        return reporte;
    }

    @Override
    public Optional<ReporteConsolidado> buscarPorFecha(LocalDate fecha) {
        return guardados.stream().filter(r -> r.getFecha().equals(fecha)).findFirst();
    }

    @Override
    public List<ReporteConsolidado> listarTodos() {
        return List.copyOf(guardados);
    }
}
