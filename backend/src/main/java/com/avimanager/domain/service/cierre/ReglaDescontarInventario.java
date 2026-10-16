package com.avimanager.domain.service.cierre;

import com.avimanager.domain.event.StockCriticoDetectado;
import com.avimanager.domain.model.Insumo;
import com.avimanager.domain.model.TipoInsumo;
import com.avimanager.domain.policy.PoliticaStock;
import com.avimanager.domain.port.in.CerrarTurnoResult;
import com.avimanager.domain.port.out.InventarioRepositoryPort;
import com.avimanager.domain.port.out.PublicadorEventosPort;

import java.util.Optional;

/**
 * F-03 / RN-03: eslabon de la cadena de cierre (patron CHAIN OF RESPONSIBILITY)
 * que descuenta de la bodega los kilos de alimento consumidos en el turno. Si el
 * stock cruza el umbral del 50%, publica StockCriticoDetectado (patron OBSERVER)
 * para que el Administrador y el Dueño sean notificados.
 *
 * Nunca detiene el cierre: siempre pasa al siguiente eslabon. Va despues de
 * ReglaCamposObligatorios (el consumo ya esta validado) y antes de ReglaMortalidad,
 * para que el alimento se descuente aunque el turno quede bloqueado por una
 * alerta sanitaria (ese turno lo cierra luego el veterinario, sin pasar por la cadena).
 */
public class ReglaDescontarInventario extends ReglaCierreTurno {

    private final InventarioRepositoryPort inventarioRepository;
    private final PublicadorEventosPort publicadorEventos;

    public ReglaDescontarInventario(InventarioRepositoryPort inventarioRepository,
                                    PublicadorEventosPort publicadorEventos) {
        this.inventarioRepository = inventarioRepository;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    protected Optional<CerrarTurnoResult> evaluar(ContextoCierreTurno contexto) {
        Double consumoKg = contexto.getComando().getConsumoAlimentoKg();
        Optional<Insumo> alimento = inventarioRepository.buscarPorTipo(TipoInsumo.ALIMENTO);
        if (consumoKg == null || alimento.isEmpty()) {
            return Optional.empty();
        }

        Insumo insumo = alimento.get();
        double stockAnterior = insumo.getStock();
        insumo.descontar(consumoKg);
        inventarioRepository.guardar(insumo);

        if (PoliticaStock.cruzaUmbralCritico(stockAnterior, insumo.getStock(), insumo.getCapacidad())) {
            publicadorEventos.publicar(new StockCriticoDetectado(insumo, stockAnterior, contexto.getAhora()));
        }
        return Optional.empty();
    }
}
