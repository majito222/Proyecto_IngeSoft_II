package com.avimanager.domain.port.in;

import com.avimanager.domain.model.Galpon;

/** F-11.1: el Administrador registra un galpon nuevo. */
public interface RegistrarGalponUseCase {

    Galpon registrarGalpon(RegistrarGalponCommand comando);
}
