package com.tecno.Smartparking.service.comando;

import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.service.DispositivoService;
import org.springframework.stereotype.Component;

/** Factory Method: centraliza la construccion de los comandos. */
@Component
public class FabricaComandos {

    private final DispositivoService dispositivo;

    public FabricaComandos(DispositivoService dispositivo) {
        this.dispositivo = dispositivo;
    }

    public Comando abrirBarrera(TipoPunto punto) {
        return new ComandoAbrirBarrera(dispositivo, punto);
    }

    public Comando cerrarBarrera(TipoPunto punto) {
        return new ComandoCerrarBarrera(dispositivo, punto);
    }

    public Comando activarAlarma() {
        return new ComandoActivarAlarma(dispositivo);
    }

    public Comando silenciarAlarma() {
        return new ComandoSilenciarAlarma(dispositivo);
    }

    public Comando actualizarPantalla(int cuposDisponibles) {
        return new ComandoActualizarPantalla(dispositivo, cuposDisponibles);
    }
}
