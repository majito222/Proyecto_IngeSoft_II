package com.avimanager.domain.port.in;

/**
 * Comando de entrada para el caso de uso "Cerrar turno diario" (F-01).
 * Los campos son nullable a proposito: la validacion de completitud (RN-05)
 * es responsabilidad del caso de uso, no de este objeto de transporte.
 */
public class CerrarTurnoCommand {

    private final String loteId;
    private final Double consumoAlimentoKg;
    private final Integer cantidadBajas;
    private final String causaProbableMortalidad;
    private final Integer produccionHuevosBandejas;
    private final String novedades;

    public CerrarTurnoCommand(String loteId, Double consumoAlimentoKg, Integer cantidadBajas,
                               String causaProbableMortalidad, Integer produccionHuevosBandejas,
                               String novedades) {
        this.loteId = loteId;
        this.consumoAlimentoKg = consumoAlimentoKg;
        this.cantidadBajas = cantidadBajas;
        this.causaProbableMortalidad = causaProbableMortalidad;
        this.produccionHuevosBandejas = produccionHuevosBandejas;
        this.novedades = novedades;
    }

    public String getLoteId() {
        return loteId;
    }

    public Double getConsumoAlimentoKg() {
        return consumoAlimentoKg;
    }

    public Integer getCantidadBajas() {
        return cantidadBajas;
    }

    public String getCausaProbableMortalidad() {
        return causaProbableMortalidad;
    }

    public Integer getProduccionHuevosBandejas() {
        return produccionHuevosBandejas;
    }

    public String getNovedades() {
        return novedades;
    }
}
