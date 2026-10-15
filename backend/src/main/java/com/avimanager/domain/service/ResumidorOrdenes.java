package com.avimanager.domain.service;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.ResumenOrden;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Arma el ResumenOrden con los nombres y el plazo calculado con el reloj. Lo
 * comparten los servicios de F-08.
 */
class ResumidorOrdenes {

    private final GalponRepositoryPort galponRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final Clock clock;

    ResumidorOrdenes(GalponRepositoryPort galponRepository, UsuarioRepositoryPort usuarioRepository, Clock clock) {
        this.galponRepository = galponRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    ResumenOrden resumir(OrdenMantenimiento orden) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        Galpon galpon = galponRepository.buscarPorId(orden.getGalponId()).orElse(null);
        String tecnico = orden.getTecnico() == null ? null : nombreDe(orden.getTecnico());
        return new ResumenOrden(orden,
                galpon == null ? orden.getGalponId() : galpon.getNombre(),
                galpon == null ? null : galpon.getEstado(),
                nombreDe(orden.getReportadoPor()), tecnico,
                orden.estaVencida(ahora), orden.minutosRestantes(ahora));
    }

    private String nombreDe(String username) {
        return usuarioRepository.buscarPorUsername(username).map(Usuario::getNombreCompleto).orElse(username);
    }
}
