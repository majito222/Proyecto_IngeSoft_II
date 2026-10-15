package com.avimanager.domain.exception;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;

/**
 * F-11: un galpon aloja un solo lote activo a la vez. Para registrar otro hay
 * que finalizar primero el actual.
 */
public class GalponConLoteActivoException extends RuntimeException {

    public GalponConLoteActivoException(Galpon galpon, Lote loteActivo) {
        super("El " + galpon.getNombre() + " ya aloja el lote activo " + loteActivo.getId()
                + ". Finalízalo antes de registrar uno nuevo.");
    }
}
