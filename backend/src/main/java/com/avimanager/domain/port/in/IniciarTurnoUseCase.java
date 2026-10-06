package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Turno;

/**
 * Puerto de entrada: F-10.2 "Iniciar turno desde el galpon asignado"
 * (CU de negocio "Gestionar turno diario", paso 1).
 */
public interface IniciarTurnoUseCase {

    Turno iniciarTurno(IniciarTurnoCommand comando);
}
