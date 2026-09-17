package com.tecno.Smartparking.exception;

/** Error de reglas de negocio, que la capa web traduce a un codigo HTTP. */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
