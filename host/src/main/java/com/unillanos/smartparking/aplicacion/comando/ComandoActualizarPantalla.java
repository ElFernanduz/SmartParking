package com.unillanos.smartparking.aplicacion.comando;

import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;

public class ComandoActualizarPantalla implements Comando {

    private final PuertoPasarelaDispositivo pasarela;
    private final int cuposDisponibles;

    public ComandoActualizarPantalla(PuertoPasarelaDispositivo pasarela, int cuposDisponibles) {
        this.pasarela = pasarela;
        this.cuposDisponibles = cuposDisponibles;
    }

    @Override
    public void ejecutar() {
        pasarela.actualizarPantalla(cuposDisponibles);
    }

    @Override
    public String descripcion() {
        return "actualizar pantalla a " + cuposDisponibles + " cupos";
    }
}
