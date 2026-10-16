package com.avimanager.domain.policy;

import com.avimanager.domain.model.DiferenciaInventario;
import com.avimanager.domain.model.Insumo;

import java.util.List;
import java.util.Map;

/**
 * RN-06 - Cuadre obligatorio de stock fisico vs. sistema: lo que hay en la
 * bodega debe coincidir al 100% con el saldo del sistema. Si al contar hay
 * diferencia, el cierre administrativo del dia se bloquea hasta que el
 * Administrador la justifique con un Acta de Ajuste (F-05).
 *
 * La tolerancia solo absorbe errores de redondeo de los decimales.
 */
public final class PoliticaConciliacion {

    private static final double TOLERANCIA = 0.001;

    private PoliticaConciliacion() {
    }

    /**
     * Compara el conteo fisico con el stock de cada insumo y devuelve los que no
     * cuadran (lista vacia si todo cuadra). Exige el conteo de todos los insumos.
     */
    public static List<DiferenciaInventario> diferencias(List<Insumo> insumos, Map<String, Double> conteoFisico) {
        Map<String, Double> conteo = conteoFisico == null ? Map.of() : conteoFisico;
        for (Insumo insumo : insumos) {
            Double contado = conteo.get(insumo.getId());
            if (contado == null) {
                throw new IllegalArgumentException("Falta el conteo físico de " + insumo.getNombre());
            }
            if (contado < 0) {
                throw new IllegalArgumentException("El conteo físico de " + insumo.getNombre() + " no puede ser negativo");
            }
        }
        return insumos.stream()
                .filter(insumo -> Math.abs(conteo.get(insumo.getId()) - insumo.getStock()) > TOLERANCIA)
                .map(insumo -> new DiferenciaInventario(insumo, conteo.get(insumo.getId())))
                .toList();
    }
}
