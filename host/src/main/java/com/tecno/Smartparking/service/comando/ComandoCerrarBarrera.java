package com.tecno.Smartparking.service.comando;

import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.service.DispositivoService;

public class ComandoCerrarBarrera implements Comando {

    private final DispositivoService dispositivo;
    private final TipoPunto punto;

    public ComandoCerrarBarrera(DispositivoService dispositivo, TipoPunto punto) {
        this.dispositivo = dispositivo;
        this.punto = punto;
    }

    @Override
    public void ejecutar() {
        dispositivo.cerrarBarrera(punto);
    }

    @Override
    public String descripcion() {
        return "cerrar barrera de " + punto;
    }
}
