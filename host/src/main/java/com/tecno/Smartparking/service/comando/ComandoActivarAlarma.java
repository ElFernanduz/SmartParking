package com.tecno.Smartparking.service.comando;

import com.tecno.Smartparking.service.DispositivoService;

public class ComandoActivarAlarma implements Comando {

    private final DispositivoService dispositivo;

    public ComandoActivarAlarma(DispositivoService dispositivo) {
        this.dispositivo = dispositivo;
    }

    @Override
    public void ejecutar() {
        dispositivo.activarAlarma();
    }

    @Override
    public String descripcion() {
        return "activar alarma";
    }
}
