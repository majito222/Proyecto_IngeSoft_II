package com.avimanager.domain.service.cierre;

import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.ReporteDiarioRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;

import java.util.Optional;

/**
 * Ultimo eslabon de la cadena: si ninguna regla anterior detuvo el cierre,
 * descuenta las bajas del lote, consolida el reporte y cierra el turno.
 * Siempre devuelve un resultado, por eso no admite un eslabon siguiente.
 */
public class ReglaConsolidacion extends ReglaCierreTurno {

    private final LoteRepositoryPort loteRepository;
    private final ReporteDiarioRepositoryPort reporteDiarioRepository;
    private final TurnoRepositoryPort turnoRepository;

    public ReglaConsolidacion(LoteRepositoryPort loteRepository,
                              ReporteDiarioRepositoryPort reporteDiarioRepository,
                              TurnoRepositoryPort turnoRepository) {
        this.loteRepository = loteRepository;
        this.reporteDiarioRepository = reporteDiarioRepository;
        this.turnoRepository = turnoRepository;
    }

    @Override
    protected Optional<CerrarTurnoResult> evaluar(ContextoCierreTurno contexto) {
        Lote lote = contexto.getLote();
        lote.registrarBajas(contexto.getComando().getCantidadBajas());
        loteRepository.guardar(lote);

        ReporteDiario reporte = contexto.reporte();
        reporte.consolidar();
        reporteDiarioRepository.guardar(reporte);

        Turno turno = contexto.getTurno();
        turno.cerrar(reporte, contexto.getAhora());
        turnoRepository.guardar(turno);

        return Optional.of(CerrarTurnoResult.turnoCerrado(reporte, turno));
    }

    @Override
    public ReglaCierreTurno enlazar(ReglaCierreTurno siguiente) {
        throw new IllegalStateException("ReglaConsolidacion siempre es el ultimo eslabon de la cadena de cierre");
    }
}
