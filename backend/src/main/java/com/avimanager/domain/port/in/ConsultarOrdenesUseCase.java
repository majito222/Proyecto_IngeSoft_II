package com.avimanager.domain.port.in;

import java.util.List;

/**
 * Puerto de entrada de F-08.3: bandeja del tecnico. Primero las pendientes, la
 * de plazo mas proximo arriba; despues las certificadas, la mas reciente arriba.
 */
public interface ConsultarOrdenesUseCase {

    List<ResumenOrden> listarOrdenes();
}
