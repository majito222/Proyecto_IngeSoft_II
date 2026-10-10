package com.avimanager.domain.port.in;

import java.util.List;

/**
 * Puerto de entrada: F-06 "Calcular racion diaria de alimento".
 */
public interface CalcularRacionUseCase {

    /** Racion de hoy de todos los lotes, ordenada por galpon. */
    List<RacionDiaria> racionesDelDia();

    RacionDiaria racionDelLote(String loteId);
}
