package com.avimanager.domain.service;

import com.avimanager.domain.exception.PesajeNoEncontradoException;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.port.in.ResumenPesaje;
import com.avimanager.domain.port.in.RevisarPesajeCommand;
import com.avimanager.domain.port.in.RevisarPesajeUseCase;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.PesajeRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Implementa la revision del Zootecnista (F-07, RN-02). Si aprueba el ajuste,
 * SelectorEstrategiaRacion pasa a usar la racion ajustada por peso para ese
 * lote mientras este sea su ultimo pesaje.
 */
public class RevisarPesajeService implements RevisarPesajeUseCase {

    private final PesajeRepositoryPort pesajeRepository;
    private final ResumidorPesajes resumidor;
    private final Clock clock;

    public RevisarPesajeService(PesajeRepositoryPort pesajeRepository, GalponRepositoryPort galponRepository,
                                UsuarioRepositoryPort usuarioRepository, Clock clock) {
        this.pesajeRepository = pesajeRepository;
        this.resumidor = new ResumidorPesajes(galponRepository, usuarioRepository);
        this.clock = clock;
    }

    @Override
    public ResumenPesaje revisar(RevisarPesajeCommand comando) {
        Pesaje pesaje = pesajeRepository.buscarPorId(comando.getPesajeId())
                .orElseThrow(() -> new PesajeNoEncontradoException(comando.getPesajeId()));
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (comando.isAprobarAjuste()) {
            pesaje.aprobarAjuste(comando.getZootecnistaUsername(), ahora);
        } else {
            pesaje.descartarAjuste(comando.getZootecnistaUsername(), ahora);
        }
        pesajeRepository.guardar(pesaje);
        return resumidor.resumir(pesaje);
    }
}
