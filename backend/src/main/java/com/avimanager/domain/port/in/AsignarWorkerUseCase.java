package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Usuario;

/**
 * F-11.1: el Administrador asigna un worker (operario) a un galpon. Un worker
 * tiene un solo galpon y no se reasigna mientras tenga un turno abierto.
 */
public interface AsignarWorkerUseCase {

    Usuario asignarWorker(AsignarWorkerCommand comando);
}
