package com.avimanager.domain.port.in;

/**
 * Puerto de entrada: F-02.5 "Atender alerta sanitaria y cerrar el turno bloqueado".
 */
public interface AtenderAlertaSanitariaUseCase {

    ResumenAlerta atender(AtenderAlertaCommand comando);
}
