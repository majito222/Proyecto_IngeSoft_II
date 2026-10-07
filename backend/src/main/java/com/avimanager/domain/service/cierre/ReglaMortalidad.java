package com.avimanager.domain.service.cierre;

import com.avimanager.domain.event.AlertaSanitariaGenerada;
import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.policy.PoliticaMortalidad;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import com.avimanager.domain.port.out.AlertaSanitariaRepositoryPort;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.PublicadorEventosPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * RN-07 calcula el % de mortalidad del dia y RN-01 bloquea el cierre si supera
 * el umbral critico: el reporte y el turno quedan bloqueados, el lote pasa a
 * observacion y se publica AlertaSanitariaGenerada (patron Observer) para que
 * los notificadores del Veterinario y del Administrador avisen. Si no supera el
 * umbral, deja el % en el reporte y pasa al siguiente eslabon.
 */
public class ReglaMortalidad extends ReglaCierreTurno {

    private final LoteRepositoryPort loteRepository;
    private final ReporteDiarioRepositoryPort reporteDiarioRepository;
    private final AlertaSanitariaRepositoryPort alertaSanitariaRepository;
    private final TurnoRepositoryPort turnoRepository;
    private final PublicadorEventosPort publicadorEventos;

    public ReglaMortalidad(LoteRepositoryPort loteRepository,
                           ReporteDiarioRepositoryPort reporteDiarioRepository,
                           AlertaSanitariaRepositoryPort alertaSanitariaRepository,
                           TurnoRepositoryPort turnoRepository,
                           PublicadorEventosPort publicadorEventos) {
        this.loteRepository = loteRepository;
        this.reporteDiarioRepository = reporteDiarioRepository;
        this.alertaSanitariaRepository = alertaSanitariaRepository;
        this.turnoRepository = turnoRepository;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    protected Optional<CerrarTurnoResult> evaluar(ContextoCierreTurno contexto) {
        Lote lote = contexto.getLote();
        ReporteDiario reporte = contexto.reporte();

        double porcentajeMortalidad = PoliticaMortalidad.calcularPorcentaje(
                contexto.getComando().getCantidadBajas(), lote.getPoblacionInicialDia());
        reporte.registrarPorcentajeMortalidad(porcentajeMortalidad);

        if (!PoliticaMortalidad.superaUmbralCritico(porcentajeMortalidad)) {
            return Optional.empty();
        }

        reporte.bloquearPorAlertaSanitaria(porcentajeMortalidad);
        reporteDiarioRepository.guardar(reporte);

        lote.ponerEnObservacion();
        loteRepository.guardar(lote);

        AlertaSanitaria alerta = new AlertaSanitaria(UUID.randomUUID().toString(), lote.getId(), reporte.getId(),
                contexto.getAhora(), porcentajeMortalidad);
        alertaSanitariaRepository.guardar(alerta);

        Turno turno = contexto.getTurno();
        turno.bloquearPorAlertaSanitaria(reporte, alerta);
        turnoRepository.guardar(turno);

        publicadorEventos.publicar(new AlertaSanitariaGenerada(alerta, lote));

        return Optional.of(CerrarTurnoResult.bloqueadoPorAlertaSanitaria(reporte, alerta, turno));
    }
}
