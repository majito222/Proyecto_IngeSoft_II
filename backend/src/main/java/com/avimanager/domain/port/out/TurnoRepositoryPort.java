package com.avimanager.domain.port.out;

import com.avimanager.domain.model.Turno;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TurnoRepositoryPort {

    Turno guardar(Turno turno);

    Optional<Turno> buscarPorLoteYFecha(String loteId, LocalDate fecha);

    List<Turno> listarTodos();

    /** Turno que quedo bloqueado por esa alerta sanitaria (F-02.5). */
    Optional<Turno> buscarPorAlertaSanitaria(String alertaId);
}
