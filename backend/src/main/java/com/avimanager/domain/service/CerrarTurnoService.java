package com.avimanager.domain.service;

import com.avimanager.domain.exception.LoteNoEncontradoException;
import com.avimanager.domain.exception.TurnoAjenoException;
import com.avimanager.domain.exception.TurnoNoDisponibleException;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import com.avimanager.domain.port.in.CerrarTurnoUseCase;
import com.avimanager.domain.port.out.LoteRepositoryPort;
import com.avimanager.domain.port.out.TurnoRepositoryPort;
import com.avimanager.domain.service.cierre.ContextoCierreTurno;
import com.avimanager.domain.service.cierre.ReglaCierreTurno;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implementa F-01 (RN-05) y F-02 (RN-07 / RN-01) en un unico flujo, tal como
 * lo describe la especificacion del caso de uso de negocio "Gestionar turno diario":
 * el registro de mortalidad es un subflujo del cierre de turno.
 *
 * El servicio solo verifica las precondiciones del cierre (F-10.1): el turno de
 * hoy existe, el solicitante es su responsable y esta ABIERTO. Las reglas de
 * negocio del cierre las aplica la cadena de ReglaCierreTurno (patron Chain of
 * Responsibility), que se arma en UseCaseConfig.
 *
 * Esta clase no conoce Spring, HTTP ni la forma en que se persisten los datos:
 * solo depende de los puertos del dominio, por lo que es 100% testeable con
 * dobles de prueba simples.
 */
public class CerrarTurnoService implements CerrarTurnoUseCase {

    private final LoteRepositoryPort loteRepository;
    private final TurnoRepositoryPort turnoRepository;
    private final ReglaCierreTurno reglasDeCierre;
    private final Clock clock;

    public CerrarTurnoService(LoteRepositoryPort loteRepository,
                               TurnoRepositoryPort turnoRepository,
                               ReglaCierreTurno reglasDeCierre,
                               Clock clock) {
        this.loteRepository = loteRepository;
        this.turnoRepository = turnoRepository;
        this.reglasDeCierre = reglasDeCierre;
        this.clock = clock;
    }

    @Override
    public CerrarTurnoResult cerrarTurno(CerrarTurnoCommand comando) {
        Lote lote = loteRepository.buscarPorId(comando.getLoteId())
                .orElseThrow(() -> new LoteNoEncontradoException(comando.getLoteId()));

        Turno turno = turnoRepository.buscarPorLoteYFecha(lote.getId(), LocalDate.now(clock))
                .orElseThrow(() -> new TurnoNoDisponibleException("Debes iniciar el turno de hoy antes de cerrarlo"));
        if (!turno.esResponsable(comando.getSolicitanteUsername())) {
            throw new TurnoAjenoException(turno.getWorkerUsername());
        }
        if (!turno.estaAbierto()) {
            throw new TurnoNoDisponibleException("El turno de hoy ya no está abierto (estado " + turno.getEstado() + ")");
        }

        return reglasDeCierre.aplicar(new ContextoCierreTurno(comando, lote, turno, LocalDateTime.now(clock)));
    }
}
