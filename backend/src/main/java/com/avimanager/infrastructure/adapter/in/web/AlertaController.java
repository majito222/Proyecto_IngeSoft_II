package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.AtenderAlertaCommand;
import com.avimanager.domain.port.in.AtenderAlertaSanitariaUseCase;
import com.avimanager.domain.port.in.ConsultarAlertasUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.AlertaResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.AtenderAlertaRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP de las alertas sanitarias:
 *   F-02.6  listado de alertas (veterinario, dueno, administrador)
 *   F-02.5  el veterinario atiende una alerta; queda registrado con el usuario del token
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final ConsultarAlertasUseCase consultarAlertasUseCase;
    private final AtenderAlertaSanitariaUseCase atenderAlertaUseCase;

    public AlertaController(ConsultarAlertasUseCase consultarAlertasUseCase,
                            AtenderAlertaSanitariaUseCase atenderAlertaUseCase) {
        this.consultarAlertasUseCase = consultarAlertasUseCase;
        this.atenderAlertaUseCase = atenderAlertaUseCase;
    }

    @GetMapping
    public List<AlertaResponse> listarAlertas() {
        return consultarAlertasUseCase.listarAlertas().stream().map(AlertaResponse::desde).toList();
    }

    @PostMapping("/{alertaId}/atencion")
    public AlertaResponse atender(@PathVariable String alertaId, @RequestBody AtenderAlertaRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        return AlertaResponse.desde(atenderAlertaUseCase.atender(new AtenderAlertaCommand(
                alertaId, jwt.getSubject(), request.getDiagnostico(), request.getTratamiento(), request.isLiberarLote())));
    }
}
