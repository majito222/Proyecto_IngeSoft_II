package com.avimanager.domain.service;

import com.avimanager.domain.exception.OrdenNoEncontradaException;
import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.port.in.AtenderOrdenUseCase;
import com.avimanager.domain.port.in.ResumenOrden;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.OrdenMantenimientoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Implementa F-08.3: el tecnico inicia y certifica la reparacion (las
 * transiciones las decide EstadoOrden, patron State). Al certificar, si el
 * galpon ya no tiene otras ordenes pendientes, vuelve a ACTIVO.
 */
public class AtenderOrdenService implements AtenderOrdenUseCase {

    private final OrdenMantenimientoRepositoryPort ordenRepository;
    private final GalponRepositoryPort galponRepository;
    private final ResumidorOrdenes resumidor;
    private final Clock clock;

    public AtenderOrdenService(OrdenMantenimientoRepositoryPort ordenRepository, GalponRepositoryPort galponRepository,
                               UsuarioRepositoryPort usuarioRepository, Clock clock) {
        this.ordenRepository = ordenRepository;
        this.galponRepository = galponRepository;
        this.resumidor = new ResumidorOrdenes(galponRepository, usuarioRepository, clock);
        this.clock = clock;
    }

    @Override
    public ResumenOrden iniciarReparacion(String ordenId, String tecnicoUsername) {
        OrdenMantenimiento orden = buscar(ordenId);
        orden.iniciarReparacion(tecnicoUsername, LocalDateTime.now(clock));
        ordenRepository.guardar(orden);
        return resumidor.resumir(orden);
    }

    @Override
    public ResumenOrden certificar(String ordenId, String tecnicoUsername, String notaReparacion) {
        OrdenMantenimiento orden = buscar(ordenId);
        orden.certificar(tecnicoUsername, notaReparacion, LocalDateTime.now(clock));
        ordenRepository.guardar(orden);

        if (ordenRepository.listarPendientesPorGalpon(orden.getGalponId()).isEmpty()) {
            galponRepository.buscarPorId(orden.getGalponId()).ifPresent(galpon -> {
                galpon.restablecer();
                galponRepository.guardar(galpon);
            });
        }
        return resumidor.resumir(orden);
    }

    private OrdenMantenimiento buscar(String ordenId) {
        return ordenRepository.buscarPorId(ordenId).orElseThrow(() -> new OrdenNoEncontradaException(ordenId));
    }
}
