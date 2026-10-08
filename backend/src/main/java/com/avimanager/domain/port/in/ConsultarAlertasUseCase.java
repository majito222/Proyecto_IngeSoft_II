package com.avimanager.domain.port.in;

import java.util.List;

/**
 * Puerto de entrada: F-02.6, listado de alertas sanitarias (la mas reciente primero).
 */
public interface ConsultarAlertasUseCase {

    List<ResumenAlerta> listarAlertas();
}
