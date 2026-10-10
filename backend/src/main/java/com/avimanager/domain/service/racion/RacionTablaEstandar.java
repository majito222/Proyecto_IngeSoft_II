package com.avimanager.domain.service.racion;

import com.avimanager.domain.model.Lote;

import java.util.Map;

/**
 * Estrategia de racion por tabla de nutricion estandar (F-06.1): gramos de
 * alimento por ave y por dia segun la semana de edad del lote.
 *
 * Valores de referencia de las guias de manejo de gallinas ponedoras
 * comerciales (levante semanas 1-19, produccion desde la semana 20). Son datos
 * de demostracion del proyecto y se pueden reemplazar por la tabla real de la
 * granja sin cambiar el resto del sistema.
 */
public class RacionTablaEstandar implements EstrategiaRacion {

    public static final int EDAD_MINIMA_SEMANAS = 1;
    public static final int EDAD_MAXIMA_SEMANAS = 80;
    private static final int INICIO_PRODUCCION_SEMANAS = 20;
    private static final double GRAMOS_EN_PRODUCCION = 110;

    private static final Map<Integer, Double> GRAMOS_POR_SEMANA_LEVANTE = Map.ofEntries(
            Map.entry(1, 10.0), Map.entry(2, 17.0), Map.entry(3, 23.0), Map.entry(4, 29.0),
            Map.entry(5, 35.0), Map.entry(6, 40.0), Map.entry(7, 45.0), Map.entry(8, 50.0),
            Map.entry(9, 54.0), Map.entry(10, 57.0), Map.entry(11, 60.0), Map.entry(12, 63.0),
            Map.entry(13, 66.0), Map.entry(14, 69.0), Map.entry(15, 72.0), Map.entry(16, 75.0),
            Map.entry(17, 80.0), Map.entry(18, 85.0), Map.entry(19, 95.0));

    @Override
    public double gramosPorAve(Lote lote) {
        int semanas = lote.getEdadSemanas();
        if (semanas < EDAD_MINIMA_SEMANAS || semanas > EDAD_MAXIMA_SEMANAS) {
            throw new IllegalArgumentException("La edad del lote " + lote.getId() + " (" + semanas
                    + " semanas) está fuera de la tabla de nutrición (semanas " + EDAD_MINIMA_SEMANAS
                    + " a " + EDAD_MAXIMA_SEMANAS + ")");
        }
        if (semanas >= INICIO_PRODUCCION_SEMANAS) {
            return GRAMOS_EN_PRODUCCION;
        }
        return GRAMOS_POR_SEMANA_LEVANTE.get(semanas);
    }

    @Override
    public String nombre() {
        return "Tabla de nutrición estándar";
    }
}
