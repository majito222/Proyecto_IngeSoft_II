package com.avimanager.domain.port.in;

/**
 * Puerto de entrada de F-07.1: el worker pesa la muestra de su galpon asignado.
 */
public interface RegistrarPesajeUseCase {

    ReferenciaPesaje referenciaDelWorker(String workerUsername);

    ResumenPesaje registrar(RegistrarPesajeCommand comando);
}
