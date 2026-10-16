package com.avimanager.domain.port.in;

/**
 * Datos de entrada del caso de uso F-05 (estilo de port/in). La operacion en si
 * es ComandoAjusteInventario, el patron Command.
 */
public class AjustarInventarioCommand {

    private final String insumoId;
    private final Double stockContado;
    private final String justificacion;
    private final String administradorUsername;

    public AjustarInventarioCommand(String insumoId, Double stockContado, String justificacion,
                                    String administradorUsername) {
        this.insumoId = insumoId;
        this.stockContado = stockContado;
        this.justificacion = justificacion;
        this.administradorUsername = administradorUsername;
    }

    public String getInsumoId() {
        return insumoId;
    }

    public Double getStockContado() {
        return stockContado;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public String getAdministradorUsername() {
        return administradorUsername;
    }
}
