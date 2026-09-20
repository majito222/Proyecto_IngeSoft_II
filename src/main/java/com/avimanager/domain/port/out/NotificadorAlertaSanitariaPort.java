package com.avimanager.domain.port.out;

import com.avimanager.domain.model.AlertaSanitaria;
import com.avimanager.domain.model.Lote;

/**
 * Puerto de salida para el canal de notificacion (push/SMS) al Veterinario
 * cuando se dispara una alerta sanitaria (RN-01). La implementacion real
 * (adaptador) puede cambiarse sin afectar el dominio.
 */
public interface NotificadorAlertaSanitariaPort {

    void notificarAlVeterinario(AlertaSanitaria alerta, Lote lote);
}
