package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.ConsultarInventarioUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.InsumoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP del inventario (F-03.2): la bodega con el stock de
 * cada insumo. Exige VER_INVENTARIO (Administrador y Dueño, ver SecurityConfig).
 */
@RestController
public class InventarioController {

    private final ConsultarInventarioUseCase consultarInventarioUseCase;

    public InventarioController(ConsultarInventarioUseCase consultarInventarioUseCase) {
        this.consultarInventarioUseCase = consultarInventarioUseCase;
    }

    @GetMapping("/api/inventario")
    public List<InsumoResponse> listarInsumos() {
        return consultarInventarioUseCase.listarInsumos().stream().map(InsumoResponse::desde).toList();
    }
}
