package com.tecno.Smartparking.service;

import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.websocket.CodificadorMensajes;
import com.tecno.Smartparking.websocket.SesionDispositivo;
import org.springframework.stereotype.Service;

/** Salida hacia la ESP32: cada accion sale como un mensaje del protocolo. */
@Service
public class DispositivoService {

    private final SesionDispositivo sesion;
    private final CodificadorMensajes codificador;

    public DispositivoService(SesionDispositivo sesion, CodificadorMensajes codificador) {
        this.sesion = sesion;
        this.codificador = codificador;
    }

    public void abrirBarrera(TipoPunto punto) {
        sesion.enviar(codificador.abrir(punto));
    }

    public void cerrarBarrera(TipoPunto punto) {
        sesion.enviar(codificador.cerrar(punto));
    }

    public void activarAlarma() {
        sesion.enviar(codificador.alarma(true));
    }

    public void silenciarAlarma() {
        sesion.enviar(codificador.alarma(false));
    }

    public void actualizarPantalla(int cuposDisponibles) {
        sesion.enviar(codificador.pantalla(cuposDisponibles));
    }

    public void configurarUmbral(int umbral) {
        sesion.enviar(codificador.configurarUmbral(umbral));
    }

    public boolean estaConectado() {
        return sesion.estaAbierta();
    }
}
