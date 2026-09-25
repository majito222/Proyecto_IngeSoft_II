package com.avimanager.domain.port.in;

/**
 * Puerto de entrada: caso de uso F-09 "Autenticar usuarios y gestionar roles".
 */
public interface IniciarSesionUseCase {

    SesionIniciada iniciarSesion(IniciarSesionCommand comando);
}
