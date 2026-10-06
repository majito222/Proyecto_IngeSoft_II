package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Galpon;
import com.avimanager.domain.model.Lote;

/**
 * Lo que ve el worker al entrar a "Cierre de turno": su galpon asignado, el lote
 * que aloja y el turno de hoy de ese lote (null si todavia no se ha iniciado).
 */
public class TurnoDelWorker {

    private final Galpon galpon;
    private final Lote lote;
    private final ResumenTurno turnoDeHoy;

    public TurnoDelWorker(Galpon galpon, Lote lote, ResumenTurno turnoDeHoy) {
        this.galpon = galpon;
        this.lote = lote;
        this.turnoDeHoy = turnoDeHoy;
    }

    public Galpon getGalpon() {
        return galpon;
    }

    public Lote getLote() {
        return lote;
    }

    public ResumenTurno getTurnoDeHoy() {
        return turnoDeHoy;
    }
}
