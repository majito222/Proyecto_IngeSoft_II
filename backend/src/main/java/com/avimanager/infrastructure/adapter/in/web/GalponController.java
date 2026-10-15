package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.AsignarWorkerCommand;
import com.avimanager.domain.port.in.AsignarWorkerUseCase;
import com.avimanager.domain.port.in.ConsultarGalponesUseCase;
import com.avimanager.domain.port.in.FinalizarLoteUseCase;
import com.avimanager.domain.port.in.RegistrarGalponCommand;
import com.avimanager.domain.port.in.RegistrarGalponUseCase;
import com.avimanager.domain.port.in.RegistrarLoteCommand;
import com.avimanager.domain.port.in.RegistrarLoteUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.AsignarGalponRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.GalponResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.LoteResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.RegistrarGalponRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.RegistrarLoteRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.WorkerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP de F-11: galpones, lotes y asignacion de workers.
 * Consultar exige VER_LOTES; registrar, finalizar y asignar exigen
 * GESTIONAR_GALPONES (ver SecurityConfig).
 */
@RestController
public class GalponController {

    private final ConsultarGalponesUseCase consultarGalponesUseCase;
    private final RegistrarGalponUseCase registrarGalponUseCase;
    private final RegistrarLoteUseCase registrarLoteUseCase;
    private final FinalizarLoteUseCase finalizarLoteUseCase;
    private final AsignarWorkerUseCase asignarWorkerUseCase;

    public GalponController(ConsultarGalponesUseCase consultarGalponesUseCase,
                            RegistrarGalponUseCase registrarGalponUseCase,
                            RegistrarLoteUseCase registrarLoteUseCase,
                            FinalizarLoteUseCase finalizarLoteUseCase,
                            AsignarWorkerUseCase asignarWorkerUseCase) {
        this.consultarGalponesUseCase = consultarGalponesUseCase;
        this.registrarGalponUseCase = registrarGalponUseCase;
        this.registrarLoteUseCase = registrarLoteUseCase;
        this.finalizarLoteUseCase = finalizarLoteUseCase;
        this.asignarWorkerUseCase = asignarWorkerUseCase;
    }

    @GetMapping("/api/galpones")
    public List<GalponResponse> listarGalpones() {
        return consultarGalponesUseCase.listarGalpones().stream().map(GalponResponse::desde).toList();
    }

    @PostMapping("/api/galpones")
    @ResponseStatus(HttpStatus.CREATED)
    public GalponResponse registrarGalpon(@RequestBody RegistrarGalponRequest request) {
        return GalponResponse.desde(registrarGalponUseCase.registrarGalpon(
                new RegistrarGalponCommand(request.getNombre(), request.getCapacidadMaxima())));
    }

    @PostMapping("/api/galpones/{galponId}/lotes")
    @ResponseStatus(HttpStatus.CREATED)
    public LoteResponse registrarLote(@PathVariable String galponId, @RequestBody RegistrarLoteRequest request) {
        return LoteResponse.desde(registrarLoteUseCase.registrarLote(
                new RegistrarLoteCommand(galponId, request.getPoblacionInicial(), request.getEdadSemanas())));
    }

    @PostMapping("/api/lotes/{loteId}/finalizacion")
    public LoteResponse finalizarLote(@PathVariable String loteId) {
        return LoteResponse.desde(finalizarLoteUseCase.finalizarLote(loteId));
    }

    @GetMapping("/api/workers")
    public List<WorkerResponse> listarWorkers() {
        return consultarGalponesUseCase.listarWorkers().stream().map(WorkerResponse::desde).toList();
    }

    @PutMapping("/api/usuarios/{username}/galpon")
    public WorkerResponse asignarGalpon(@PathVariable String username, @RequestBody AsignarGalponRequest request) {
        return WorkerResponse.desde(asignarWorkerUseCase.asignarWorker(
                new AsignarWorkerCommand(username, request.getGalponId())));
    }
}
