package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import com.avimanager.domain.port.in.CerrarTurnoUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.CerrarTurnoRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.CerrarTurnoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada HTTP para F-01 (cierre de turno, RN-05) y F-02
 * (alerta sanitaria de mortalidad, RN-01/RN-07), ambas parte del mismo
 * caso de uso de negocio "Gestionar turno diario".
 */
@RestController
@RequestMapping("/api/lotes/{loteId}/turnos")
public class TurnoController {

    private final CerrarTurnoUseCase cerrarTurnoUseCase;

    public TurnoController(CerrarTurnoUseCase cerrarTurnoUseCase) {
        this.cerrarTurnoUseCase = cerrarTurnoUseCase;
    }

    @PostMapping("/cierre")
    public ResponseEntity<CerrarTurnoResponse> cerrarTurno(@PathVariable String loteId,
                                                            @RequestBody CerrarTurnoRequest request) {
        CerrarTurnoCommand comando = new CerrarTurnoCommand(
                loteId,
                request.getConsumoAlimentoKg(),
                request.getCantidadBajas(),
                request.getCausaProbableMortalidad(),
                request.getProduccionHuevosBandejas(),
                request.getNovedades());

        CerrarTurnoResult resultado = cerrarTurnoUseCase.cerrarTurno(comando);
        CerrarTurnoResponse body = CerrarTurnoResponse.desde(resultado);

        HttpStatus status = switch (resultado.getEstado()) {
            case CAMPOS_INCOMPLETOS -> HttpStatus.UNPROCESSABLE_ENTITY;
            case ALERTA_SANITARIA -> HttpStatus.CONFLICT;
            case TURNO_CERRADO -> HttpStatus.OK;
        };

        return ResponseEntity.status(status).body(body);
    }
}
