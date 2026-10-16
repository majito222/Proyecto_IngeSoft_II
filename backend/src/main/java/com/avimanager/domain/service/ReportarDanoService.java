package com.avimanager.domain.service;

import com.avimanager.domain.event.DanoReportado;
import com.avimanager.domain.exception.GalponNoAsignadoException;
import com.avimanager.domain.exception.GalponNoEncontradoException;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.OrdenMantenimiento;
import com.avimanager.domain.model.TipoDano;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.GalponParaReporte;
import com.avimanager.domain.port.in.ReportarDanoCommand;
import com.avimanager.domain.port.in.ReportarDanoUseCase;
import com.avimanager.domain.port.in.ResumenOrden;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.OrdenMantenimientoRepositoryPort;
import com.avimanager.domain.port.out.PublicadorEventosPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;
import com.avimanager.domain.service.mantenimiento.FabricaOrdenMantenimiento;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Implementa F-08.1 / F-08.2: al reportar un dano, el galpon pasa a
 * EN_RIESGO_SANITARIO y FabricaOrdenMantenimiento (patron Factory) crea la orden
 * con su prioridad y el plazo de 12 horas (RN-04). Despues publica
 * DanoReportado (patron Observer) para avisar al Tecnico y al Administrador.
 *
 * El dano lo reporta el worker que lo detecta en su galpon asignado (Student
 * Book, proceso Mantenimiento): el galpon no se elige, sale de su asignacion.
 */
public class ReportarDanoService implements ReportarDanoUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final GalponRepositoryPort galponRepository;
    private final OrdenMantenimientoRepositoryPort ordenRepository;
    private final FabricaOrdenMantenimiento fabrica;
    private final PublicadorEventosPort publicadorEventos;
    private final ResumidorOrdenes resumidor;
    private final Clock clock;

    public ReportarDanoService(UsuarioRepositoryPort usuarioRepository, GalponRepositoryPort galponRepository,
                               OrdenMantenimientoRepositoryPort ordenRepository, FabricaOrdenMantenimiento fabrica,
                               PublicadorEventosPort publicadorEventos, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.galponRepository = galponRepository;
        this.ordenRepository = ordenRepository;
        this.fabrica = fabrica;
        this.publicadorEventos = publicadorEventos;
        this.resumidor = new ResumidorOrdenes(galponRepository, usuarioRepository, clock);
        this.clock = clock;
    }

    @Override
    public List<GalponParaReporte> galponesParaReporte(String solicitanteUsername) {
        String asignado = galponAsignadoDe(solicitanteUsername);
        if (asignado == null) {
            return List.of();
        }
        return galponRepository.buscarPorId(asignado).stream()
                .map(g -> new GalponParaReporte(g.getId(), g.getNombre(), g.getEstado(),
                        ordenRepository.listarPendientesPorGalpon(g.getId()).stream()
                                .sorted(Comparator.comparing(OrdenMantenimiento::getFechaLimite))
                                .map(resumidor::resumir)
                                .toList()))
                .toList();
    }

    @Override
    public ResumenOrden reportar(ReportarDanoCommand comando) {
        String galponId = galponAsignadoDe(comando.getSolicitanteUsername());
        if (galponId == null) {
            throw new GalponNoAsignadoException("Tu usuario no tiene un galpón asignado, así que no puede reportar daños.");
        }
        Galpon galpon = galponRepository.buscarPorId(galponId)
                .orElseThrow(() -> new GalponNoEncontradoException(galponId));

        OrdenMantenimiento orden = fabrica.crear(galponId, tipoDe(comando.getTipoDano()), comando.getDescripcion(),
                comando.getSolicitanteUsername(), LocalDateTime.now(clock));
        galpon.marcarEnRiesgoSanitario();

        ordenRepository.guardar(orden);
        galponRepository.guardar(galpon);
        publicadorEventos.publicar(new DanoReportado(orden, galpon));
        return resumidor.resumir(orden);
    }

    private String galponAsignadoDe(String username) {
        return usuarioRepository.buscarPorUsername(username).map(Usuario::getGalponAsignadoId).orElse(null);
    }

    private static TipoDano tipoDe(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de daño es obligatorio");
        }
        try {
            return TipoDano.valueOf(tipo);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de daño desconocido: " + tipo);
        }
    }
}
