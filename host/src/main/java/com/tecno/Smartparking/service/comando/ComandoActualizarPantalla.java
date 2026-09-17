package com.tecno.Smartparking.service.comando;

import com.tecno.Smartparking.service.DispositivoService;

public class ComandoActualizarPantalla implements Comando {

    private final DispositivoService dispositivo;
    private final int cuposDisponibles;

    public ComandoActualizarPantalla(DispositivoService dispositivo, int cuposDisponibles) {
        this.dispositivo = dispositivo;
        this.cuposDisponibles = cuposDisponibles;
    }

    @Override
    public void ejecutar() {
        dispositivo.actualizarPantalla(cuposDisponibles);
    }

    @Override
    public String descripcion() {
        return "actualizar pantalla a " + cuposDisponibles + " cupos";
    }
}
