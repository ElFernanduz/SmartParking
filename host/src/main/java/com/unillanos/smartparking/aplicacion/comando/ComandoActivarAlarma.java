package com.unillanos.smartparking.aplicacion.comando;

import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;

public class ComandoActivarAlarma implements Comando {

    private final PuertoPasarelaDispositivo pasarela;

    public ComandoActivarAlarma(PuertoPasarelaDispositivo pasarela) {
        this.pasarela = pasarela;
    }

    @Override
    public void ejecutar() {
        pasarela.activarAlarma();
    }

    @Override
    public String descripcion() {
        return "activar alarma";
    }
}
