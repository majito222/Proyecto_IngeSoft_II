package com.avimanager.domain.policy;

/**
 * RN-07 - Calculo de Porcentaje de Mortalidad Diaria:
 *   % Mortalidad = (Aves Muertas Hoy / Poblacion Inicial del Dia) X 100
 *
 * RN-01 - Alerta de Mortalidad Maxima: si el porcentaje supera el umbral
 * critico, el sistema debe bloquear el cierre operativo y notificar.
 */
public final class PoliticaMortalidad {

    public static final double UMBRAL_CRITICO_PORCENTAJE = 1.5;

    private PoliticaMortalidad() {
    }

    public static double calcularPorcentaje(int avesMuertasHoy, int poblacionInicialDelDia) {
        if (poblacionInicialDelDia <= 0) {
            throw new IllegalArgumentException("La poblacion inicial del dia debe ser mayor a cero");
        }
        if (avesMuertasHoy < 0) {
            throw new IllegalArgumentException("La cantidad de aves muertas no puede ser negativa");
        }
        return (avesMuertasHoy / (double) poblacionInicialDelDia) * 100.0;
    }

    public static boolean superaUmbralCritico(double porcentajeMortalidad) {
        return porcentajeMortalidad > UMBRAL_CRITICO_PORCENTAJE;
    }
}
