package com.unillanos.smartparking.aplicacion.comando;

import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;

public class ComandoSilenciarAlarma implements Comando {

    private final PuertoPasarelaDispositivo pasarela;

    public ComandoSilenciarAlarma(PuertoPasarelaDispositivo pasarela) {
        this.pasarela = pasarela;
    }

    @Override
    public void ejecutar() {
        pasarela.silenciarAlarma();
    }

    @Override
    public String descripcion() {
        return "silenciar alarma";
    }
}
