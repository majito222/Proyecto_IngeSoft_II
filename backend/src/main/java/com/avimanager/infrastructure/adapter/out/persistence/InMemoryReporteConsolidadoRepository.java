package com.avimanager.infrastructure.adapter.out.persistence;

import com.avimanager.domain.model.ReporteConsolidado;
import com.avimanager.domain.port.out.ReporteConsolidadoRepositoryPort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Adaptador de salida: reportes consolidados en memoria, uno por fecha (F-04.1). */
@Repository
public class InMemoryReporteConsolidadoRepository implements ReporteConsolidadoRepositoryPort {

    private final Map<LocalDate, ReporteConsolidado> almacen = new ConcurrentHashMap<>();

    @Override
    public ReporteConsolidado guardar(ReporteConsolidado reporte) {
        almacen.put(reporte.getFecha(), reporte);
        return reporte;
    }

    @Override
    public Optional<ReporteConsolidado> buscarPorFecha(LocalDate fecha) {
        return Optional.ofNullable(almacen.get(fecha));
    }

    @Override
    public List<ReporteConsolidado> listarTodos() {
        return List.copyOf(almacen.values());
    }
}
