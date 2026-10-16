package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.ConsolidarReporteCommand;
import com.avimanager.domain.port.in.ConsolidarReporteDiarioUseCase;
import com.avimanager.domain.port.in.ConsultarKpisUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.ConsolidarReporteRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.KpisLoteResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.ReporteConsolidadoResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador de entrada HTTP de los KPIs (ver SecurityConfig):
 *   F-04.2  KPIs del dia de todos los lotes y reportes consolidados (VER_DASHBOARD_KPI: Dueño y Administrador)
 *   F-04.1  consolidar el reporte del dia (APROBAR_REPORTES: Administrador)
 */
@RestController
public class KpiController {

    private final ConsultarKpisUseCase consultarKpisUseCase;
    private final ConsolidarReporteDiarioUseCase consolidarReporteUseCase;
    private final Clock clock;

    public KpiController(ConsultarKpisUseCase consultarKpisUseCase,
                         ConsolidarReporteDiarioUseCase consolidarReporteUseCase,
                         Clock clock) {
        this.consultarKpisUseCase = consultarKpisUseCase;
        this.consolidarReporteUseCase = consolidarReporteUseCase;
        this.clock = clock;
    }

    @GetMapping("/api/kpis")
    public List<KpisLoteResponse> kpisDelDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now(clock);
        return consultarKpisUseCase.calcularKpis(dia).stream().map(KpisLoteResponse::desde).toList();
    }

    @GetMapping("/api/kpis/consolidados")
    public List<ReporteConsolidadoResponse> reportesConsolidados() {
        return consultarKpisUseCase.listarConsolidados().stream().map(ReporteConsolidadoResponse::desde).toList();
    }

    @PostMapping("/api/kpis/consolidacion")
    @ResponseStatus(HttpStatus.CREATED)
    public ReporteConsolidadoResponse consolidar(@RequestBody ConsolidarReporteRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return ReporteConsolidadoResponse.desde(consolidarReporteUseCase.consolidar(
                new ConsolidarReporteCommand(request.getFecha(), jwt.getSubject())));
    }
}
