package com.avimanager.domain.port.in;

/**
 * Comando de F-07: el Zootecnista decide si se ajusta la racion del lote
 * (aprobarAjuste = true) o si se mantiene la estandar.
 */
public class RevisarPesajeCommand {

    private final String pesajeId;
    private final String zootecnistaUsername;
    private final boolean aprobarAjuste;

    public RevisarPesajeCommand(String pesajeId, String zootecnistaUsername, boolean aprobarAjuste) {
        this.pesajeId = pesajeId;
        this.zootecnistaUsername = zootecnistaUsername;
        this.aprobarAjuste = aprobarAjuste;
    }

    public String getPesajeId() {
        return pesajeId;
    }

    public String getZootecnistaUsername() {
        return zootecnistaUsername;
    }

    public boolean isAprobarAjuste() {
        return aprobarAjuste;
    }
}
