package com.avimanager.domain.service;

import com.avimanager.domain.event.PesajeFueraDeRango;
import com.avimanager.domain.exception.PesajeYaRegistradoException;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.domain.policy.PoliticaPeso;
import com.avimanager.domain.port.in.ReferenciaPesaje;
import com.avimanager.domain.port.in.RegistrarPesajeCommand;
import com.avimanager.domain.port.in.RegistrarPesajeUseCase;
import com.avimanager.domain.port.in.ResumenPesaje;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.PesajeRepositoryPort;
import com.avimanager.domain.port.out.PublicadorEventosPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementa F-07.1: el worker pesa una muestra de aves de su galpon asignado y
 * el sistema la compara con el peso estandar de la semana (RN-02). Si se
 * desvia mas de +-5%, el pesaje queda pendiente de revision y se publica
 * PesajeFueraDeRango (patron Observer) para avisar al Zootecnista.
 *
 * El pesaje es semanal: un lote tiene un solo pesaje por semana calendario.
 */
public class RegistrarPesajeService implements RegistrarPesajeUseCase {

    private final GalponAsignadoResolver galponAsignado;
    private final PesajeRepositoryPort pesajeRepository;
    private final PublicadorEventosPort publicadorEventos;
    private final ResumidorPesajes resumidor;
    private final Clock clock;

    public RegistrarPesajeService(UsuarioRepositoryPort usuarioRepository, GalponRepositoryPort galponRepository,
                                  LoteRepositoryPort loteRepository, PesajeRepositoryPort pesajeRepository,
                                  PublicadorEventosPort publicadorEventos, Clock clock) {
        this.galponAsignado = new GalponAsignadoResolver(usuarioRepository, galponRepository, loteRepository);
        this.pesajeRepository = pesajeRepository;
        this.publicadorEventos = publicadorEventos;
        this.resumidor = new ResumidorPesajes(galponRepository, usuarioRepository);
        this.clock = clock;
    }

    @Override
    public ReferenciaPesaje referenciaDelWorker(String workerUsername) {
        GalponAsignadoResolver.Asignacion asignacion = galponAsignado.resolver(workerUsername);
        Lote lote = asignacion.lote();
        ResumenPesaje pesajeDeLaSemana = pesajeDeEstaSemana(lote.getId()).map(resumidor::resumir).orElse(null);
        return new ReferenciaPesaje(asignacion.galpon().getId(), asignacion.galpon().getNombre(), lote.getId(),
                lote.getEdadSemanas(), lote.getPoblacionActual(),
                PoliticaPeso.pesoEstandarGramos(lote.getEdadSemanas()), pesajeDeLaSemana);
    }

    @Override
    public ResumenPesaje registrar(RegistrarPesajeCommand comando) {
        if (comando.getPesoPromedioGramos() == null) {
            throw new IllegalArgumentException("El peso promedio de la muestra es obligatorio");
        }
        if (comando.getTamanoMuestra() == null) {
            throw new IllegalArgumentException("El tamaño de la muestra es obligatorio");
        }
        GalponAsignadoResolver.Asignacion asignacion = galponAsignado.resolver(comando.getWorkerUsername());
        Lote lote = asignacion.lote();
        if (pesajeDeEstaSemana(lote.getId()).isPresent()) {
            throw new PesajeYaRegistradoException("El lote del " + asignacion.galpon().getNombre()
                    + " ya se pesó esta semana. El próximo pesaje es a partir del lunes.");
        }

        Pesaje pesaje = new Pesaje(UUID.randomUUID().toString(), lote, comando.getPesoPromedioGramos(),
                comando.getTamanoMuestra(), comando.getWorkerUsername(), LocalDateTime.now(clock));
        pesajeRepository.guardar(pesaje);

        if (pesaje.estaFueraDeRango()) {
            publicadorEventos.publicar(new PesajeFueraDeRango(pesaje));
        }
        return resumidor.resumir(pesaje);
    }

    private Optional<Pesaje> pesajeDeEstaSemana(String loteId) {
        LocalDate lunes = LocalDate.now(clock).with(DayOfWeek.MONDAY);
        return pesajeRepository.listarPorLote(loteId).stream()
                .filter(p -> p.getFecha().with(DayOfWeek.MONDAY).equals(lunes))
                .findFirst();
    }
}
