package com.avimanager.domain.service.cierre;

import com.avimanager.domain.port.in.CerrarTurnoCommand;
import com.avimanager.domain.port.in.CerrarTurnoResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RN-05: el turno solo se cierra con el reporte completo. Si falta algun campo
 * obligatorio, termina la cadena y el turno sigue ABIERTO.
 */
public class ReglaCamposObligatorios extends ReglaCierreTurno {

    @Override
    protected Optional<CerrarTurnoResult> evaluar(ContextoCierreTurno contexto) {
        List<String> faltantes = camposFaltantes(contexto.getComando());
        if (faltantes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(CerrarTurnoResult.camposIncompletos(faltantes, contexto.getTurno()));
    }

    private List<String> camposFaltantes(CerrarTurnoCommand comando) {
        List<String> faltantes = new ArrayList<>();
        if (comando.getConsumoAlimentoKg() == null) {
            faltantes.add("consumoAlimentoKg");
        }
        if (comando.getCantidadBajas() == null) {
            faltantes.add("cantidadBajas");
        }
        if (comando.getCantidadBajas() != null && comando.getCantidadBajas() > 0
                && (comando.getCausaProbableMortalidad() == null || comando.getCausaProbableMortalidad().isBlank())) {
            faltantes.add("causaProbableMortalidad");
        }
        if (comando.getProduccionHuevosBandejas() == null) {
            faltantes.add("produccionHuevosBandejas");
        }
        return faltantes;
    }
}
