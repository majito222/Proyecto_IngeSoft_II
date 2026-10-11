package com.avimanager.infrastructure.adapter.in.web;

import com.avimanager.domain.port.in.ConsultarPesajesUseCase;
import com.avimanager.domain.port.in.RegistrarPesajeCommand;
import com.avimanager.domain.port.in.RegistrarPesajeUseCase;
import com.avimanager.domain.port.in.RevisarPesajeCommand;
import com.avimanager.domain.port.in.RevisarPesajeUseCase;
import com.avimanager.infrastructure.adapter.in.web.dto.PesajeResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.ReferenciaPesajeResponse;
import com.avimanager.infrastructure.adapter.in.web.dto.RegistrarPesajeRequest;
import com.avimanager.infrastructure.adapter.in.web.dto.RevisarPesajeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP del pesaje semanal (F-07):
 *   F-07.1  el worker consulta la referencia y registra el pesaje de su galpon (REGISTRAR_PESO)
 *   F-07.2  el Zootecnista lista los pesajes y revisa los fuera de rango (REVISAR_PESAJES)
 */
@RestController
@RequestMapping("/api/pesajes")
public class PesajeController {

    private final RegistrarPesajeUseCase registrarPesajeUseCase;
    private final RevisarPesajeUseCase revisarPesajeUseCase;
    private final ConsultarPesajesUseCase consultarPesajesUseCase;

    public PesajeController(RegistrarPesajeUseCase registrarPesajeUseCase,
                            RevisarPesajeUseCase revisarPesajeUseCase,
                            ConsultarPesajesUseCase consultarPesajesUseCase) {
        this.registrarPesajeUseCase = registrarPesajeUseCase;
        this.revisarPesajeUseCase = revisarPesajeUseCase;
        this.consultarPesajesUseCase = consultarPesajesUseCase;
    }

    @GetMapping("/referencia")
    public ReferenciaPesajeResponse referencia(@AuthenticationPrincipal Jwt jwt) {
        return ReferenciaPesajeResponse.desde(registrarPesajeUseCase.referenciaDelWorker(jwt.getSubject()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PesajeResponse registrar(@RequestBody RegistrarPesajeRequest request, @AuthenticationPrincipal Jwt jwt) {
        return PesajeResponse.desde(registrarPesajeUseCase.registrar(new RegistrarPesajeCommand(
                jwt.getSubject(), request.getPesoPromedioGramos(), request.getTamanoMuestra())));
    }

    @GetMapping
    public List<PesajeResponse> listar() {
        return consultarPesajesUseCase.listarPesajes().stream().map(PesajeResponse::desde).toList();
    }

    @PostMapping("/{pesajeId}/revision")
    public PesajeResponse revisar(@PathVariable String pesajeId, @RequestBody RevisarPesajeRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        return PesajeResponse.desde(revisarPesajeUseCase.revisar(
                new RevisarPesajeCommand(pesajeId, jwt.getSubject(), request.isAprobarAjuste())));
    }
}
