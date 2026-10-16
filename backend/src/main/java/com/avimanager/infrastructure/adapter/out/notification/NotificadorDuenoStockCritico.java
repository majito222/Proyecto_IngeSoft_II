package com.avimanager.infrastructure.adapter.out.notification;

import com.avimanager.domain.event.StockCriticoDetectado;
import com.avimanager.domain.model.Insumo;
import com.avimanager.infrastructure.adapter.out.event.SuscriptorEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador de StockCriticoDetectado (RN-03): avisa a el Dueño, que aprueba la inversion.
 * Es un suscriptor mas del patron Observer; el eslabon que descuenta el
 * inventario no sabe a quien se notifica.
 */
@Component
public class NotificadorDuenoStockCritico implements SuscriptorEvento<StockCriticoDetectado> {

    private static final Logger log = LoggerFactory.getLogger(NotificadorDuenoStockCritico.class);

    @Override
    public Class<StockCriticoDetectado> tipoDeEvento() {
        return StockCriticoDetectado.class;
    }

    @Override
    public void alRecibir(StockCriticoDetectado evento) {
        Insumo insumo = evento.getInsumo();
        log.warn("[NOTIFICACION PUSH/SMS -> DUEÑO] Stock Crítico de {}: quedan {} {} de {} ({}% de la bodega). "
                        + "Hay que reabastecer.",
                insumo.getNombre(), String.format("%.1f", insumo.getStock()), insumo.getUnidad(),
                String.format("%.0f", insumo.getCapacidad()), String.format("%.1f", insumo.porcentajeDisponible()));
    }
}
