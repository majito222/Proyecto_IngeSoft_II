package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Turno;

/**
 * F-04.3: el Administrador aprueba el reporte de un turno CERRADO o
 * CERRADO_CON_ALERTA, que pasa a APROBADO a su nombre.
 */
public interface AprobarReporteUseCase {

    Turno aprobarReporte(AprobarReporteCommand comando);
}
