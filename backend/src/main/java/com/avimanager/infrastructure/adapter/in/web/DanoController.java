package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.ReportarDanoCommand;
import com.avimanager.domain.port.in.ReportarDanoUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.GalponParaReporteResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.OrdenMantenimientoResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.ReportarDanoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP de F-08.2: reportar danos de infraestructura
 * (operario y administrador; permiso REPORTAR_DANOS).
 */
@RestController
@RequestMapping("/api/danos")
public class DanoController {

    private final ReportarDanoUseCase reportarDanoUseCase;

    public DanoController(ReportarDanoUseCase reportarDanoUseCase) {
        this.reportarDanoUseCase = reportarDanoUseCase;
    }

    @GetMapping("/referencia")
    public List<GalponParaReporteResponse> galponesParaReporte(@AuthenticationPrincipal Jwt jwt) {
        return reportarDanoUseCase.galponesParaReporte(jwt.getSubject()).stream()
                .map(GalponParaReporteResponse::desde)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenMantenimientoResponse reportar(@RequestBody ReportarDanoRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        return OrdenMantenimientoResponse.desde(reportarDanoUseCase.reportar(new ReportarDanoCommand(
                jwt.getSubject(), request.getGalponId(), request.getTipoDano(), request.getDescripcion())));
    }
}
