package com.avimanager.domain.exception;

/**
 * El pesaje es semanal: un lote tiene un solo pesaje por semana calendario (F-07).
 */
public class PesajeYaRegistradoException extends RuntimeException {

    public PesajeYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
