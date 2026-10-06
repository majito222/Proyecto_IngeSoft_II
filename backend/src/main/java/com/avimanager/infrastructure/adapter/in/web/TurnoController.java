package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.model.EstadoTurno;
import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import com.avimanager.domain.port.in.CerrarTurnoUseCase;
import com.avimanager.domain.port.in.ConsultarTurnosUseCase;
import com.avimanager.domain.port.in.IniciarTurnoCommand;
import com.avimanager.domain.port.in.IniciarTurnoUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.CerrarTurnoRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.CerrarTurnoResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.TurnoDelWorkerResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.TurnoResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador de entrada HTTP del turno:
 *   F-01 / F-02  cierre de turno (RN-05, RN-01/RN-07), parte del CU "Gestionar turno diario"
 *   F-10.2       el worker consulta e inicia el turno de hoy en su galpon asignado
 *   F-10.3       dashboard de cierres pendientes del administrador
 */
@RestController
public class TurnoController {

    private final CerrarTurnoUseCase cerrarTurnoUseCase;
    private final IniciarTurnoUseCase iniciarTurnoUseCase;
    private final ConsultarTurnosUseCase consultarTurnosUseCase;

    public TurnoController(CerrarTurnoUseCase cerrarTurnoUseCase, IniciarTurnoUseCase iniciarTurnoUseCase,
                           ConsultarTurnosUseCase consultarTurnosUseCase) {
        this.cerrarTurnoUseCase = cerrarTurnoUseCase;
        this.iniciarTurnoUseCase = iniciarTurnoUseCase;
        this.consultarTurnosUseCase = consultarTurnosUseCase;
    }

    @PostMapping("/api/lotes/{loteId}/turnos/cierre")
    public ResponseEntity<CerrarTurnoResponse> cerrarTurno(@PathVariable String loteId,
                                                            @RequestBody CerrarTurnoRequest request,
                                                            @AuthenticationPrincipal Jwt jwt) {
        CerrarTurnoCommand comando = new CerrarTurnoCommand(
                loteId,
                request.getConsumoAlimentoKg(),
                request.getCantidadBajas(),
                request.getCausaProbableMortalidad(),
                request.getProduccionHuevosBandejas(),
                request.getNovedades(),
                jwt.getSubject());

        CerrarTurnoResult resultado = cerrarTurnoUseCase.cerrarTurno(comando);
        CerrarTurnoResponse body = CerrarTurnoResponse.desde(resultado);

        HttpStatus status = switch (resultado.getEstado()) {
            case CAMPOS_INCOMPLETOS -> HttpStatus.UNPROCESSABLE_ENTITY;
            case ALERTA_SANITARIA -> HttpStatus.CONFLICT;
            case TURNO_CERRADO -> HttpStatus.OK;
        };

        return ResponseEntity.status(status).body(body);
    }

    @GetMapping("/api/turnos/actual")
    public TurnoDelWorkerResponse turnoActual(@AuthenticationPrincipal Jwt jwt) {
        return TurnoDelWorkerResponse.desde(consultarTurnosUseCase.consultarTurnoDelWorker(jwt.getSubject()));
    }

    @PostMapping("/api/turnos/inicio")
    public ResponseEntity<TurnoDelWorkerResponse> iniciarTurno(@AuthenticationPrincipal Jwt jwt) {
        iniciarTurnoUseCase.iniciarTurno(new IniciarTurnoCommand(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TurnoDelWorkerResponse.desde(consultarTurnosUseCase.consultarTurnoDelWorker(jwt.getSubject())));
    }

    @GetMapping("/api/turnos")
    public List<TurnoResponse> listarTurnos(
            @RequestParam(required = false) EstadoTurno estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return consultarTurnosUseCase.listarTurnos(estado, fecha).stream().map(TurnoResponse::desde).toList();
    }
}
