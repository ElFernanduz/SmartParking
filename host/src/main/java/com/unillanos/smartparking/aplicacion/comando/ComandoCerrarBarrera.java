package com.unillanos.smartparking.aplicacion.comando;

import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;

public class ComandoCerrarBarrera implements Comando {

    private final PuertoPasarelaDispositivo pasarela;
    private final TipoPunto punto;

    public ComandoCerrarBarrera(PuertoPasarelaDispositivo pasarela, TipoPunto punto) {
        this.pasarela = pasarela;
        this.punto = punto;
    }

    @Override
    public void ejecutar() {
        pasarela.cerrarBarrera(punto);
    }

    @Override
    public String descripcion() {
        return "cerrar barrera de " + punto;
    }
}
