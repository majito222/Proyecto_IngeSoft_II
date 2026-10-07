package com.avimanager.domain.service.cierre;

import com.avimanager.domain.model.Lote;
import com.avimanager.domain.model.ReporteDiario;
import com.avimanager.domain.model.Turno;
import com.avimanager.domain.port.in.CerrarTurnoCommand;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Datos que viajan por la cadena de reglas del cierre de turno. Cada eslabon lee
 * lo que necesita y deja en el reporte lo que calcula (por ejemplo, el % de
 * mortalidad) para los siguientes.
 */
public class ContextoCierreTurno {

    private final CerrarTurnoCommand comando;
    private final Lote lote;
    private final Turno turno;
    private final LocalDateTime ahora;
    private ReporteDiario reporte;

    public ContextoCierreTurno(CerrarTurnoCommand comando, Lote lote, Turno turno, LocalDateTime ahora) {
        this.comando = comando;
        this.lote = lote;
        this.turno = turno;
        this.ahora = ahora;
    }

    /** El reporte del turno se arma la primera vez que un eslabon lo pide. */
    public ReporteDiario reporte() {
        if (reporte == null) {
            reporte = new ReporteDiario(
                    UUID.randomUUID().toString(),
                    lote.getId(),
                    ahora.toLocalDate(),
                    comando.getConsumoAlimentoKg(),
                    comando.getCantidadBajas(),
                    comando.getCausaProbableMortalidad(),
                    comando.getProduccionHuevosBandejas(),
                    comando.getNovedades());
        }
        return reporte;
    }

    public CerrarTurnoCommand getComando() {
        return comando;
    }

    public Lote getLote() {
        return lote;
    }

    public Turno getTurno() {
        return turno;
    }

    public LocalDateTime getAhora() {
        return ahora;
    }
}
