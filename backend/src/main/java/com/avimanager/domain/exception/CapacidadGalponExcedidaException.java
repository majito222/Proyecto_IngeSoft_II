package com.avimanager.domain.exception;

import com.avimanager.domain.model.Galpon;

/**
 * F-11: la poblacion de un lote no puede superar la capacidad maxima del galpon.
 */
public class CapacidadGalponExcedidaException extends RuntimeException {

    public CapacidadGalponExcedidaException(Galpon galpon, int poblacion) {
        super("El " + galpon.getNombre() + " tiene capacidad para " + galpon.getCapacidadMaxima()
                + " aves; no puede alojar un lote de " + poblacion);
    }
}
