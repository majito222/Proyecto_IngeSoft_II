package com.avimanager.domain.service.cierre;

import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;

import java.util.Optional;

/**
 * Eslabon de la cadena de cierre que valida que los valores del reporte tengan
 * sentido antes de cualquier efecto: consumo y produccion no negativos, y bajas
 * entre 0 y las aves vivas del lote. Va justo despues de ReglaCamposObligatorios
 * y antes de ReglaDescontarInventario, para que un dato invalido nunca descuente
 * la bodega ni deje la poblacion del lote en negativo.
 *
 * Un dato invalido es un error de la peticion (400), no un resultado del cierre:
 * el turno sigue ABIERTO y el worker corrige el formulario.
 */
public class ReglaValoresValidos extends ReglaCierreTurno {

    @Override
    protected Optional<CerrarTurnoResult> evaluar(ContextoCierreTurno contexto) {
        CerrarTurnoCommand comando = contexto.getComando();
        if (comando.getConsumoAlimentoKg() < 0) {
            throw new IllegalArgumentException("El consumo de alimento no puede ser negativo");
        }
        if (comando.getProduccionHuevosBandejas() < 0) {
            throw new IllegalArgumentException("La producción de huevos no puede ser negativa");
        }
        int avesVivas = contexto.getLote().getPoblacionActual();
        if (comando.getCantidadBajas() < 0 || comando.getCantidadBajas() > avesVivas) {
            throw new IllegalArgumentException("La cantidad de bajas debe estar entre 0 y las " + avesVivas
                    + " aves vivas del lote");
        }
        return Optional.empty();
    }
}
