package com.unillanos.smartparking.infraestructura.dispositivo;

import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import org.springframework.stereotype.Component;

/** Adaptador de salida: cada accion del nucleo sale como un mensaje del protocolo. */
@Component
public class PasarelaDispositivoWebSocket implements PuertoPasarelaDispositivo {

    private final SesionDispositivo sesion;
    private final CodificadorMensajesDispositivo codificador;

    public PasarelaDispositivoWebSocket(SesionDispositivo sesion,
                                        CodificadorMensajesDispositivo codificador) {
        this.sesion = sesion;
        this.codificador = codificador;
    }

    @Override
    public void abrirBarrera(TipoPunto punto) {
        sesion.enviar(codificador.abrir(punto));
    }

    @Override
    public void cerrarBarrera(TipoPunto punto) {
        sesion.enviar(codificador.cerrar(punto));
    }

    @Override
    public void activarAlarma() {
        sesion.enviar(codificador.alarma(true));
    }

    @Override
    public void silenciarAlarma() {
        sesion.enviar(codificador.alarma(false));
    }

    @Override
    public void actualizarPantalla(int cuposDisponibles) {
        sesion.enviar(codificador.pantalla(cuposDisponibles));
    }

    @Override
    public void configurarUmbral(int umbral) {
        sesion.enviar(codificador.configurarUmbral(umbral));
    }

    @Override
    public boolean estaConectado() {
        return sesion.estaAbierta();
    }
}
