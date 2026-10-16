package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.AjustarInventarioCommand;
import com.avimanager.domain.port.in.AjustarInventarioUseCase;
import com.avimanager.domain.port.in.ConsultarActasAjusteUseCase;
import com.avimanager.domain.port.in.ConsultarInventarioUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.ActaAjusteResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.AjustarInventarioRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.InsumoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP del inventario (ver SecurityConfig):
 *   F-03.2  la bodega con el stock de cada insumo (VER_INVENTARIO: Administrador y Dueño)
 *   F-05.1  el Acta de Ajuste (AJUSTAR_INVENTARIO: solo el Administrador)
 *   F-05    el historial de actas (VER_INVENTARIO)
 */
@RestController
public class InventarioController {

    private final ConsultarInventarioUseCase consultarInventarioUseCase;
    private final AjustarInventarioUseCase ajustarInventarioUseCase;
    private final ConsultarActasAjusteUseCase consultarActasAjusteUseCase;

    public InventarioController(ConsultarInventarioUseCase consultarInventarioUseCase,
                                AjustarInventarioUseCase ajustarInventarioUseCase,
                                ConsultarActasAjusteUseCase consultarActasAjusteUseCase) {
        this.consultarInventarioUseCase = consultarInventarioUseCase;
        this.ajustarInventarioUseCase = ajustarInventarioUseCase;
        this.consultarActasAjusteUseCase = consultarActasAjusteUseCase;
    }

    @GetMapping("/api/inventario")
    public List<InsumoResponse> listarInsumos() {
        return consultarInventarioUseCase.listarInsumos().stream().map(InsumoResponse::desde).toList();
    }

    @PostMapping("/api/inventario/{insumoId}/ajustes")
    @ResponseStatus(HttpStatus.CREATED)
    public ActaAjusteResponse ajustarInventario(@PathVariable String insumoId,
                                                @RequestBody AjustarInventarioRequest request,
                                                @AuthenticationPrincipal Jwt jwt) {
        return ActaAjusteResponse.desde(ajustarInventarioUseCase.ajustarInventario(new AjustarInventarioCommand(
                insumoId, request.getStockContado(), request.getJustificacion(), jwt.getSubject())));
    }

    @GetMapping("/api/inventario/ajustes")
    public List<ActaAjusteResponse> listarActas() {
        return consultarActasAjusteUseCase.listarActas().stream().map(ActaAjusteResponse::desde).toList();
    }
}
