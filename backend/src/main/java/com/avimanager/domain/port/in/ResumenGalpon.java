package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.Usuario;

import java.util.List;

/**
 * F-11.2: un galpon con su lote activo (null si esta libre) y los workers que
 * tiene asignados.
 */
public class ResumenGalpon {

    private final Galpon galpon;
    private final Lote loteActivo;
    private final List<Usuario> workers;

    public ResumenGalpon(Galpon galpon, Lote loteActivo, List<Usuario> workers) {
        this.galpon = galpon;
        this.loteActivo = loteActivo;
        this.workers = List.copyOf(workers);
    }

    public Galpon getGalpon() {
        return galpon;
    }

    public Lote getLoteActivo() {
        return loteActivo;
    }

    public List<Usuario> getWorkers() {
        return workers;
    }
}
