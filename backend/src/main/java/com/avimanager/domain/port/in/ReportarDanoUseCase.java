package com.avimanager.domain.port.in;

import java.util.List;

/**
 * Puerto de entrada de F-08.1 / F-08.2: reportar un dano de infraestructura.
 */
public interface ReportarDanoUseCase {

    /** Galpones donde el usuario puede reportar: el suyo si es worker, todos si no tiene galpon asignado. */
    List<GalponParaReporte> galponesParaReporte(String solicitanteUsername);

    ResumenOrden reportar(ReportarDanoCommand comando);
}
