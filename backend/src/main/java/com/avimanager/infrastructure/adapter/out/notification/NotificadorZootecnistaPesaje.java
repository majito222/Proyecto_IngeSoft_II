package com.avimanager.infrastructure.adapter.out.notification;

import com.avimanager.domain.event.PesajeFueraDeRango;
import com.avimanager.domain.model.Pesaje;
import com.avimanager.infrastructure.adapter.out.event.SuscriptorEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador de PesajeFueraDeRango (RN-02): simula el aviso al Zootecnista para
 * que revise el pesaje y decida si se ajusta la racion.
 */
@Component
public class NotificadorZootecnistaPesaje implements SuscriptorEvento<PesajeFueraDeRango> {

    private static final Logger log = LoggerFactory.getLogger(NotificadorZootecnistaPesaje.class);

    @Override
    public Class<PesajeFueraDeRango> tipoDeEvento() {
        return PesajeFueraDeRango.class;
    }

    @Override
    public void alRecibir(PesajeFueraDeRango evento) {
        Pesaje pesaje = evento.getPesaje();
        log.warn("[NOTIFICACION -> ZOOTECNISTA] Lote {} (galpon {}): peso promedio {} g frente a {} g estandar "
                        + "(semana {}), desviacion {}%. Revisa el ajuste de racion.",
                pesaje.getLoteId(), pesaje.getGalponId(), pesaje.getPesoPromedioGramos(),
                pesaje.getPesoEstandarGramos(), pesaje.getEdadSemanas(),
                String.format("%+.2f", pesaje.getDesviacionPorcentaje()));
    }
}
