package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.AprobarReporteCommand;
import com.avimanager.domain.port.in.AprobarReporteUseCase;
import com.avimanager.domain.port.in.ConsultarTurnosUseCase;
import com.avimanager.domain.port.in.ResumenTurno;
import com.avimanager.domain.model.Turno;
import com.avimanager.infrastructure.adapter.in.web.dto.TurnoResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * F-04.3: el Administrador aprueba el reporte de un turno desde el Panel de
 * turnos (permiso APROBAR_REPORTES, ver SecurityConfig). Responde la fila del
 * panel ya actualizada.
 */
@RestController
public class AprobacionReporteController {

    private final AprobarReporteUseCase aprobarReporteUseCase;
    private final ConsultarTurnosUseCase consultarTurnosUseCase;

    public AprobacionReporteController(AprobarReporteUseCase aprobarReporteUseCase,
                                       ConsultarTurnosUseCase consultarTurnosUseCase) {
        this.aprobarReporteUseCase = aprobarReporteUseCase;
        this.consultarTurnosUseCase = consultarTurnosUseCase;
    }

    @PostMapping("/api/turnos/{turnoId}/aprobacion")
    public TurnoResponse aprobar(@PathVariable String turnoId, @AuthenticationPrincipal Jwt jwt) {
        Turno turno = aprobarReporteUseCase.aprobarReporte(new AprobarReporteCommand(turnoId, jwt.getSubject()));
        ResumenTurno resumen = consultarTurnosUseCase.listarTurnos(null, turno.getFecha()).stream()
                .filter(r -> r.getTurno().getId().equals(turno.getId()))
                .findFirst()
                .orElseThrow();
        return TurnoResponse.desde(resumen);
    }
}
