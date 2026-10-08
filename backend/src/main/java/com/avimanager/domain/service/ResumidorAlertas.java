package com.avimanager.domain.service;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.model.Usuario;
import com.avimanager.domain.port.in.ResumenAlerta;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

/**
 * Arma el ResumenAlerta con los nombres que necesita la pantalla. Lo comparten
 * ConsultarAlertasService y AtenderAlertaSanitariaService.
 */
class ResumidorAlertas {

    private final TurnoRepositoryPort turnoRepository;
    private final LoteRepositoryPort loteRepository;
    private final GalponRepositoryPort galponRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    ResumidorAlertas(TurnoRepositoryPort turnoRepository, LoteRepositoryPort loteRepository,
                     GalponRepositoryPort galponRepository, UsuarioRepositoryPort usuarioRepository) {
        this.turnoRepository = turnoRepository;
        this.loteRepository = loteRepository;
        this.galponRepository = galponRepository;
        this.usuarioRepository = usuarioRepository;
    }

    ResumenAlerta resumir(AlertaSanitaria alerta) {
        Turno turno = turnoRepository.buscarPorAlertaSanitaria(alerta.getId()).orElse(null);
        String galponNombre = loteRepository.buscarPorId(alerta.getLoteId())
                .flatMap(lote -> galponRepository.buscarPorId(lote.getGalponId()))
                .map(Galpon::getNombre)
                .orElse(null);
        String responsable = turno == null ? null : nombreDe(turno.getWorkerUsername());
        String atendidaPor = alerta.getAtendidaPor() == null ? null : nombreDe(alerta.getAtendidaPor());
        return new ResumenAlerta(alerta, turno, galponNombre, responsable, atendidaPor);
    }

    private String nombreDe(String username) {
        return usuarioRepository.buscarPorUsername(username).map(Usuario::getNombreCompleto).orElse(username);
    }
}
