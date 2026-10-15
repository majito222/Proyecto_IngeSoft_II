package com.avimanager.domain.port.out;

import com.avimanager.domain.model.OrdenMantenimiento;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida de las ordenes de mantenimiento (F-08).
 */
public interface OrdenMantenimientoRepositoryPort {

    OrdenMantenimiento guardar(OrdenMantenimiento orden);

    Optional<OrdenMantenimiento> buscarPorId(String ordenId);

    List<OrdenMantenimiento> listarTodas();

    /** Ordenes ABIERTAS o EN_REPARACION del galpon. */
    List<OrdenMantenimiento> listarPendientesPorGalpon(String galponId);
}
