package com.tecno.Smartparking.service.comando;

import com.tecno.Smartparking.service.DispositivoService;

public class ComandoSilenciarAlarma implements Comando {

    private final DispositivoService dispositivo;

    public ComandoSilenciarAlarma(DispositivoService dispositivo) {
        this.dispositivo = dispositivo;
    }

    @Override
    public void ejecutar() {
        dispositivo.silenciarAlarma();
    }

    @Override
    public String descripcion() {
        return "silenciar alarma";
    }
}
