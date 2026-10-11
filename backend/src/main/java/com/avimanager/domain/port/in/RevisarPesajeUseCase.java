package com.avimanager.domain.port.in;

/**
 * Puerto de entrada de F-07: el Zootecnista revisa un pesaje fuera de rango (RN-02).
 */
public interface RevisarPesajeUseCase {

    ResumenPesaje revisar(RevisarPesajeCommand comando);
}
