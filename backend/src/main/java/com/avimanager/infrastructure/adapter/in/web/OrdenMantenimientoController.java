package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.AtenderOrdenUseCase;
import com.avimanager.domain.port.in.ConsultarOrdenesUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.CertificarReparacionRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.OrdenMantenimientoResponse;
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
 * Adaptador de entrada HTTP de F-08.3: bandeja del tecnico (tecnico y
 * administrador; permiso ATENDER_ORDENES_MANTENIMIENTO). El tecnico queda
 * registrado con el usuario del token.
 */
@RestController
@RequestMapping("/api/ordenes-mantenimiento")
public class OrdenMantenimientoController {

    private final ConsultarOrdenesUseCase consultarOrdenesUseCase;
    private final AtenderOrdenUseCase atenderOrdenUseCase;

    public OrdenMantenimientoController(ConsultarOrdenesUseCase consultarOrdenesUseCase,
                                        AtenderOrdenUseCase atenderOrdenUseCase) {
        this.consultarOrdenesUseCase = consultarOrdenesUseCase;
        this.atenderOrdenUseCase = atenderOrdenUseCase;
    }

    @GetMapping
    public List<OrdenMantenimientoResponse> listar() {
        return consultarOrdenesUseCase.listarOrdenes().stream().map(OrdenMantenimientoResponse::desde).toList();
    }

    @PostMapping("/{ordenId}/inicio")
    public OrdenMantenimientoResponse iniciarReparacion(@PathVariable String ordenId, @AuthenticationPrincipal Jwt jwt) {
        return OrdenMantenimientoResponse.desde(atenderOrdenUseCase.iniciarReparacion(ordenId, jwt.getSubject()));
    }

    @PostMapping("/{ordenId}/certificacion")
    public OrdenMantenimientoResponse certificar(@PathVariable String ordenId,
                                                 @RequestBody CertificarReparacionRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return OrdenMantenimientoResponse.desde(
                atenderOrdenUseCase.certificar(ordenId, jwt.getSubject(), request.getNotaReparacion()));
    }
}
