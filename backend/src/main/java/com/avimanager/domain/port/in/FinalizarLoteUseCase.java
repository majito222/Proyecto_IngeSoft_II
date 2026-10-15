package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Lote;

/** F-11.1: el lote sale del galpon, que queda libre para recibir otro. */
public interface FinalizarLoteUseCase {

    Lote finalizarLote(String loteId);
}
