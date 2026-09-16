package com.unillanos.smartparking.aplicacion.comando;

/** Accion sobre un actuador, encapsulada como objeto. */
public interface Comando {

    void ejecutar();

    String descripcion();
}
