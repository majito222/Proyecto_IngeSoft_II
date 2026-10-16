package com.avimanager.domain.policy;

import java.util.Map;

/**
 * RN-02 - Ajuste de racion por peso: si el peso promedio de la muestra difiere
 * mas de +-5% del peso estandar de la semana, el sistema alerta para que el
 * Zootecnista apruebe el ajuste de la racion (F-07).
 *
 * Peso estandar en gramos por semana de edad: valores de referencia de las
 * guias de manejo de gallinas ponedoras comerciales. La semana 0 es el pollito
 * de un dia al llegar al galpon (unos 40 g). Son datos de demostracion del
 * proyecto y se pueden reemplazar por la tabla real de la granja.
 */
public final class PoliticaPeso {

    public static final double TOLERANCIA_PORCENTAJE = 5.0;
    public static final int EDAD_MINIMA_SEMANAS = 0;
    public static final int EDAD_MAXIMA_SEMANAS = 80;
    private static final int INICIO_PESO_ADULTO_SEMANAS = 25;
    private static final double PESO_ADULTO_GRAMOS = 1900;

    private static final Map<Integer, Double> GRAMOS_POR_SEMANA = Map.ofEntries(
            Map.entry(0, 40.0), Map.entry(1, 70.0), Map.entry(2, 120.0), Map.entry(3, 190.0), Map.entry(4, 270.0),
            Map.entry(5, 360.0), Map.entry(6, 450.0), Map.entry(7, 540.0), Map.entry(8, 630.0),
            Map.entry(9, 720.0), Map.entry(10, 810.0), Map.entry(11, 890.0), Map.entry(12, 970.0),
            Map.entry(13, 1050.0), Map.entry(14, 1120.0), Map.entry(15, 1190.0), Map.entry(16, 1260.0),
            Map.entry(17, 1340.0), Map.entry(18, 1430.0), Map.entry(19, 1520.0), Map.entry(20, 1600.0),
            Map.entry(21, 1660.0), Map.entry(22, 1720.0), Map.entry(23, 1780.0), Map.entry(24, 1840.0));

    private PoliticaPeso() {
    }

    public static double pesoEstandarGramos(int edadSemanas) {
        if (edadSemanas < EDAD_MINIMA_SEMANAS || edadSemanas > EDAD_MAXIMA_SEMANAS) {
            throw new IllegalArgumentException("La edad del lote (" + edadSemanas
                    + " semanas) está fuera de la tabla de peso estándar (semanas " + EDAD_MINIMA_SEMANAS
                    + " a " + EDAD_MAXIMA_SEMANAS + ")");
        }
        if (edadSemanas >= INICIO_PESO_ADULTO_SEMANAS) {
            return PESO_ADULTO_GRAMOS;
        }
        return GRAMOS_POR_SEMANA.get(edadSemanas);
    }

    /** Desviacion del peso real frente al estandar, en %, redondeada a 2 decimales (negativa si esta por debajo). */
    public static double calcularDesviacion(double pesoPromedioGramos, double pesoEstandarGramos) {
        double desviacion = (pesoPromedioGramos - pesoEstandarGramos) / pesoEstandarGramos * 100.0;
        return Math.round(desviacion * 100.0) / 100.0;
    }

    /** El limite es estricto: una desviacion de exactamente 5% sigue dentro de rango. */
    public static boolean fueraDeRango(double desviacionPorcentaje) {
        return Math.abs(desviacionPorcentaje) > TOLERANCIA_PORCENTAJE;
    }
}
