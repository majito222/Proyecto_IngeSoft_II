package com.avimanager.domain.port.out;

import com.avimanager.domain.model.ActaAjuste;

import java.util.List;

/** Historial de actas de ajuste (F-05): solo se agregan, nunca se modifican. */
public interface ActaAjusteRepositoryPort {

    ActaAjuste guardar(ActaAjuste acta);

    List<ActaAjuste> listarTodas();
}
