package com.avimanager.domain.service.inventario;

import com.avimanager.domain.model.ActaAjuste;

/**
 * Patron COMMAND (F-05): una operacion sobre el inventario encapsulada como un
 * objeto, con todos sus datos (que insumo, que valor, quien, cuando y por que).
 * Quien la ejecuta (AjustarInventarioService, el invocador) no sabe como se
 * aplica; solo la ejecuta y guarda el acta que devuelve, que es la auditoria.
 *
 * Ojo: no confundir con los *Command de port/in (por ejemplo,
 * AjustarInventarioCommand), que son solo los datos de entrada de un caso de uso.
 */
public interface ComandoInventario {

    /** Aplica la operacion sobre el insumo (el receptor) y devuelve el acta que la audita. */
    ActaAjuste ejecutar();
}
