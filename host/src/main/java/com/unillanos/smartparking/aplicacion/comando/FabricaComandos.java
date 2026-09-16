package com.unillanos.smartparking.aplicacion.comando;

import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;

/** Factory Method: centraliza la construccion de los comandos sobre los actuadores. */
public class FabricaComandos {

    private final PuertoPasarelaDispositivo pasarela;

    public FabricaComandos(PuertoPasarelaDispositivo pasarela) {
        this.pasarela = pasarela;
    }

    public Comando abrirBarrera(TipoPunto punto) {
        return new ComandoAbrirBarrera(pasarela, punto);
    }

    public Comando cerrarBarrera(TipoPunto punto) {
        return new ComandoCerrarBarrera(pasarela, punto);
    }

    public Comando activarAlarma() {
        return new ComandoActivarAlarma(pasarela);
    }

    public Comando silenciarAlarma() {
        return new ComandoSilenciarAlarma(pasarela);
    }

    public Comando actualizarPantalla(int cuposDisponibles) {
        return new ComandoActualizarPantalla(pasarela, cuposDisponibles);
    }
}
