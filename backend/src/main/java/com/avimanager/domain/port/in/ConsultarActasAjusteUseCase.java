package com.avimanager.domain.port.in;

import com.avimanager.domain.model.ActaAjuste;

import java.util.List;

/** F-05: historial de actas de ajuste, la mas reciente primero. */
public interface ConsultarActasAjusteUseCase {

    List<ActaAjuste> listarActas();
}
