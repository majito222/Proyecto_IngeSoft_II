package com.avimanager.domain.policy;

/**
 * RN-03: un insumo esta en "Stock Critico" cuando su stock queda por debajo del
 * 50% de la capacidad de la bodega. Justo en el 50% todavia no es critico.
 */
public final class PoliticaStock {

    public static final double UMBRAL_CRITICO_PORCENTAJE = 50.0;

    private PoliticaStock() {
    }

    public static double porcentajeDisponible(double stock, double capacidad) {
        return stock / capacidad * 100.0;
    }

    public static boolean esCritico(double stock, double capacidad) {
        return porcentajeDisponible(stock, capacidad) < UMBRAL_CRITICO_PORCENTAJE;
    }

    /**
     * El stock acaba de bajar del umbral: antes no era critico y ahora si. Asi la
     * alerta se emite una vez al cruzar el 50%, no en cada cierre posterior.
     */
    public static boolean cruzaUmbralCritico(double stockAnterior, double stockActual, double capacidad) {
        return !esCritico(stockAnterior, capacidad) && esCritico(stockActual, capacidad);
    }
}
