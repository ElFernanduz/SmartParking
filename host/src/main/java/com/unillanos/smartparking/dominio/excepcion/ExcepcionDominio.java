package com.unillanos.smartparking.dominio.excepcion;

public class ExcepcionDominio extends RuntimeException {

    public ExcepcionDominio(String mensaje) {
        super(mensaje);
    }
}
