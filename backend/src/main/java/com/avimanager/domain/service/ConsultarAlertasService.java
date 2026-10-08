package com.avimanager.domain.service;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.port.in.ConsultarAlertasUseCase;
import com.avimanager.domain.port.in.ResumenAlerta;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.util.Comparator;
import java.util.List;

/**
 * F-02.6: listado de alertas sanitarias, la mas reciente primero.
 */
public class ConsultarAlertasService implements ConsultarAlertasUseCase {

    private final AlertaSanitariaRepositoryPort alertaRepository;
    private final ResumidorAlertas resumidor;

    public ConsultarAlertasService(AlertaSanitariaRepositoryPort alertaRepository, TurnoRepositoryPort turnoRepository,
                                   LoteRepositoryPort loteRepository, GalponRepositoryPort galponRepository,
                                   UsuarioRepositoryPort usuarioRepository) {
        this.alertaRepository = alertaRepository;
        this.resumidor = new ResumidorAlertas(turnoRepository, loteRepository, galponRepository, usuarioRepository);
    }

    @Override
    public List<ResumenAlerta> listarAlertas() {
        return alertaRepository.listarTodas().stream()
                .sorted(Comparator.comparing(AlertaSanitaria::getFechaHora).reversed())
                .map(resumidor::resumir)
                .toList();
    }
}
