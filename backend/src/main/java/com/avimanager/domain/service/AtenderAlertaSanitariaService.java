package com.avimanager.domain.service;

import com.avimanager.domain.event.AlertaSanitariaAtendida;
import com.avimanager.domain.exception.AlertaNoEncontradaException;
import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.exception.TurnoNoDisponibleException;
import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.AtenderAlertaCommand;
import com.avimanager.domain.port.in.AtenderAlertaSanitariaUseCase;
import com.avimanager.domain.port.in.ResumenAlerta;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.GalponRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.PublicadorEventosPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.port.out.UsuarioRepositoryPort;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Implementa F-02.5: el veterinario atiende la alerta sanitaria (diagnostico y
 * tratamiento) y el sistema cierra el turno bloqueado como CERRADO_CON_ALERTA
 * (patron State), consolida el reporte del worker y descuenta sus bajas del
 * lote. Si el veterinario lo indica, el lote sale de observacion. Al final
 * publica AlertaSanitariaAtendida (patron Observer).
 *
 * El worker no tiene que reenviar nada: sus datos ya habian pasado RN-05 y lo
 * unico que bloqueaba el cierre era RN-01.
 */
public class AtenderAlertaSanitariaService implements AtenderAlertaSanitariaUseCase {

    private final AlertaSanitariaRepositoryPort alertaRepository;
    private final TurnoRepositoryPort turnoRepository;
    private final ReporteDiarioRepositoryPort reporteDiarioRepository;
    private final LoteRepositoryPort loteRepository;
    private final PublicadorEventosPort publicadorEventos;
    private final ResumidorAlertas resumidor;
    private final Clock clock;

    public AtenderAlertaSanitariaService(AlertaSanitariaRepositoryPort alertaRepository,
                                         TurnoRepositoryPort turnoRepository,
                                         ReporteDiarioRepositoryPort reporteDiarioRepository,
                                         LoteRepositoryPort loteRepository,
                                         GalponRepositoryPort galponRepository,
                                         UsuarioRepositoryPort usuarioRepository,
                                         PublicadorEventosPort publicadorEventos,
                                         Clock clock) {
        this.alertaRepository = alertaRepository;
        this.turnoRepository = turnoRepository;
        this.reporteDiarioRepository = reporteDiarioRepository;
        this.loteRepository = loteRepository;
        this.publicadorEventos = publicadorEventos;
        this.resumidor = new ResumidorAlertas(turnoRepository, loteRepository, galponRepository, usuarioRepository);
        this.clock = clock;
    }

    @Override
    public ResumenAlerta atender(AtenderAlertaCommand comando) {
        AlertaSanitaria alerta = alertaRepository.buscarPorId(comando.getAlertaId())
                .orElseThrow(() -> new AlertaNoEncontradaException(comando.getAlertaId()));
        Turno turno = turnoRepository.buscarPorAlertaSanitaria(alerta.getId())
                .orElseThrow(() -> new TurnoNoDisponibleException("La alerta no tiene un turno bloqueado asociado"));
        ReporteDiario reporte = reporteDiarioRepository.buscarPorId(alerta.getReporteDiarioId())
                .orElseThrow(() -> new IllegalStateException("No existe el reporte " + alerta.getReporteDiarioId()));
        Lote lote = loteRepository.buscarPorId(alerta.getLoteId())
                .orElseThrow(() -> new LoteNoEncontradoException(alerta.getLoteId()));
        LocalDateTime ahora = LocalDateTime.now(clock);

        // Primero la alerta: valida los datos y que siga pendiente antes de tocar lo demas.
        alerta.atender(comando.getDiagnostico(), comando.getTratamiento(), comando.getVeterinarioUsername(), ahora);
        turno.cerrarConAlertaAtendida(ahora);
        reporte.consolidar();
        lote.registrarBajas(reporte.getCantidadBajas());
        if (comando.isLiberarLote()) {
            lote.liberarDeObservacion();
        }

        alertaRepository.guardar(alerta);
        turnoRepository.guardar(turno);
        reporteDiarioRepository.guardar(reporte);
        loteRepository.guardar(lote);

        publicadorEventos.publicar(new AlertaSanitariaAtendida(alerta, lote, turno));

        return resumidor.resumir(alerta);
    }
}
