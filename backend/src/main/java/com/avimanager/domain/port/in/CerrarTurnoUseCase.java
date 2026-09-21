package com.avimanager.domain.port.in;

/**
 * Puerto de entrada: caso de uso F-01 "Registrar cierre de turno operativo",
 * que integra en el mismo flujo la regla RN-05 (completitud de campos) y,
 * cuando aplica, la regla RN-07/RN-01 (calculo y alerta de mortalidad) de F-02.
 */
public interface CerrarTurnoUseCase {

    CerrarTurnoResult cerrarTurno(CerrarTurnoCommand comando);
}
