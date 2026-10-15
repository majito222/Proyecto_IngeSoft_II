package com.avimanager.domain.service;

import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.port.in.ConsultarOrdenesUseCase;
import com.avimanager.domain.port.in.ResumenOrden;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.OrdenMantenimientoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Implementa la bandeja del tecnico (F-08.3): pendientes primero, la de plazo
 * mas proximo arriba (las vencidas quedan de primeras); despues las
 * certificadas, la mas reciente arriba.
 */
public class ConsultarOrdenesService implements ConsultarOrdenesUseCase {

    private final OrdenMantenimientoRepositoryPort ordenRepository;
    private final ResumidorOrdenes resumidor;

    public ConsultarOrdenesService(OrdenMantenimientoRepositoryPort ordenRepository,
                                   GalponRepositoryPort galponRepository, UsuarioRepositoryPort usuarioRepository,
                                   Clock clock) {
        this.ordenRepository = ordenRepository;
        this.resumidor = new ResumidorOrdenes(galponRepository, usuarioRepository, clock);
    }

    @Override
    public List<ResumenOrden> listarOrdenes() {
        List<OrdenMantenimiento> todas = ordenRepository.listarTodas();
        Stream<OrdenMantenimiento> pendientes = todas.stream()
                .filter(OrdenMantenimiento::estaPendiente)
                .sorted(Comparator.comparing(OrdenMantenimiento::getFechaLimite));
        Stream<OrdenMantenimiento> certificadas = todas.stream()
                .filter(o -> !o.estaPendiente())
                .sorted(Comparator.comparing(OrdenMantenimiento::getFechaCertificacion).reversed());
        return Stream.concat(pendientes, certificadas).map(resumidor::resumir).toList();
    }
}
