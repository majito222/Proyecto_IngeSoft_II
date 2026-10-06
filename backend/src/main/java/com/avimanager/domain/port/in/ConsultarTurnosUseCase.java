package com.avimanager.domain.port.in;

import com.avimanager.domain.model.EstadoTurno;

import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de entrada de consultas de F-10: el turno del dia de un worker
 * (F-10.2) y el dashboard de cierres pendientes del administrador (F-10.3).
 */
public interface ConsultarTurnosUseCase {

    TurnoDelWorker consultarTurnoDelWorker(String workerUsername);

    /** Filtros opcionales: null significa "todos". */
    List<ResumenTurno> listarTurnos(EstadoTurno estado, LocalDate fecha);
}
