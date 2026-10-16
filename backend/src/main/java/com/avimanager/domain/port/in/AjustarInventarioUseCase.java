package com.avimanager.domain.port.in;

import com.avimanager.domain.model.ActaAjuste;

/**
 * F-05.1 / RN-08: el Administrador registra un Acta de Ajuste con el stock
 * contado en la bodega; el stock del sistema pasa a ser el contado.
 */
public interface AjustarInventarioUseCase {

    ActaAjuste ajustarInventario(AjustarInventarioCommand comando);
}
