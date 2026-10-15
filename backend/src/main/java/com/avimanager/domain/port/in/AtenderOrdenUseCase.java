package com.avimanager.domain.port.in;

/**
 * Puerto de entrada de F-08.3: el tecnico inicia y certifica la reparacion.
 */
public interface AtenderOrdenUseCase {

    ResumenOrden iniciarReparacion(String ordenId, String tecnicoUsername);

    ResumenOrden certificar(String ordenId, String tecnicoUsername, String notaReparacion);
}
