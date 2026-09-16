package com.unillanos.smartparking.aplicacion.comando;

import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;

public class ComandoAbrirBarrera implements Comando {

    private final PuertoPasarelaDispositivo pasarela;
    private final TipoPunto punto;

    public ComandoAbrirBarrera(PuertoPasarelaDispositivo pasarela, TipoPunto punto) {
        this.pasarela = pasarela;
        this.punto = punto;
    }

    @Override
    public void ejecutar() {
        pasarela.abrirBarrera(punto);
    }

    @Override
    public String descripcion() {
        return "abrir barrera de " + punto;
    }
}
