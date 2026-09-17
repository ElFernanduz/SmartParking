package com.tecno.Smartparking.service.comando;

/** Accion sobre un actuador, encapsulada como objeto. */
public interface Comando {

    void ejecutar();

    String descripcion();
}
