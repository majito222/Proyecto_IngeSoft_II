package com.avimanager.domain.service.cierre;

import com.avimanager.domain.port.in.CerrarTurnoResult;

import java.util.Optional;

/**
 * Eslabon del patron CHAIN OF RESPONSIBILITY para el cierre de turno. Cada regla
 * de negocio evalua el cierre y puede terminarlo con un resultado (por ejemplo,
 * campos incompletos o alerta sanitaria) o pasarlo al siguiente eslabon.
 *
 * Cadena actual (se arma en UseCaseConfig):
 *   ReglaCamposObligatorios (RN-05) -> ReglaMortalidad (RN-07/RN-01) -> ReglaConsolidacion
 * Una regla nueva (por ejemplo, el descuento de inventario de F-03) se agrega
 * como otro eslabon antes de ReglaConsolidacion, sin modificar CerrarTurnoService.
 *
 * aplicar() es ademas un Template Method: fija el recorrido de la cadena y cada
 * regla solo implementa evaluar().
 */
public abstract class ReglaCierreTurno {

    private ReglaCierreTurno siguiente;

    /** Enlaza el siguiente eslabon y lo devuelve, para encadenar: a.enlazar(b).enlazar(c). */
    public ReglaCierreTurno enlazar(ReglaCierreTurno siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    public final CerrarTurnoResult aplicar(ContextoCierreTurno contexto) {
        Optional<CerrarTurnoResult> resultado = evaluar(contexto);
        if (resultado.isPresent()) {
            return resultado.get();
        }
        if (siguiente == null) {
            throw new IllegalStateException(
                    "La cadena de cierre termino sin resultado: el ultimo eslabon debe ser ReglaConsolidacion");
        }
        return siguiente.aplicar(contexto);
    }

    /** Devuelve un resultado para terminar el cierre, o vacio para pasar al siguiente eslabon. */
    protected abstract Optional<CerrarTurnoResult> evaluar(ContextoCierreTurno contexto);
}
