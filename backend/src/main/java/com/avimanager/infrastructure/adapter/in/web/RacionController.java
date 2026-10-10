package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.CalcularRacionUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.RacionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP de F-06.2: racion sugerida de hoy por galpon
 * (zootecnista, operario y administrador; permiso VER_RACION).
 */
@RestController
@RequestMapping("/api/racion")
public class RacionController {

    private final CalcularRacionUseCase calcularRacionUseCase;

    public RacionController(CalcularRacionUseCase calcularRacionUseCase) {
        this.calcularRacionUseCase = calcularRacionUseCase;
    }

    @GetMapping
    public List<RacionResponse> racionesDelDia() {
        return calcularRacionUseCase.racionesDelDia().stream().map(RacionResponse::desde).toList();
    }
}
