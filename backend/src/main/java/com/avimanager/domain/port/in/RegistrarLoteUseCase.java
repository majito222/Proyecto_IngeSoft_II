package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Lote;

/**
 * F-11.1: el Administrador aloja un lote nuevo en un galpon. Se rechaza si la
 * poblacion supera la capacidad del galpon o si este ya tiene un lote activo.
 */
public interface RegistrarLoteUseCase {

    Lote registrarLote(RegistrarLoteCommand comando);
}
