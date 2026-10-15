package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Usuario;

import java.util.List;

/**
 * Consultas de F-11: los galpones con su lote activo y sus workers, y la lista
 * de workers (operarios) que el Administrador puede asignar.
 */
public interface ConsultarGalponesUseCase {

    List<ResumenGalpon> listarGalpones();

    List<Usuario> listarWorkers();
}
