package com.tecno.Smartparking.service.comando;

import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.service.DispositivoService;

public class ComandoAbrirBarrera implements Comando {

    private final DispositivoService dispositivo;
    private final TipoPunto punto;

    public ComandoAbrirBarrera(DispositivoService dispositivo, TipoPunto punto) {
        this.dispositivo = dispositivo;
        this.punto = punto;
    }

    @Override
    public void ejecutar() {
        dispositivo.abrirBarrera(punto);
    }

    @Override
    public String descripcion() {
        return "abrir barrera de " + punto;
    }
}
