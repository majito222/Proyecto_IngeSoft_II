package com.avimanager.domain.port.in;

import java.util.List;

/**
 * Puerto de entrada de F-07.2: listado de pesajes, primero los pendientes de
 * revision y luego los mas recientes.
 */
public interface ConsultarPesajesUseCase {

    List<ResumenPesaje> listarPesajes();
}
