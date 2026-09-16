package com.unillanos.smartparking.infraestructura.dispositivo;

import java.io.IOException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Sesion abierta con la ESP32. Se mantiene aparte del manejador para que la
 * pasarela pueda escribir sin depender de el.
 */
@Component
public class SesionDispositivo {

    private static final Logger LOG = LoggerFactory.getLogger(SesionDispositivo.class);

    private volatile WebSocketSession sesion;
    private volatile Instant ultimoLatido = Instant.EPOCH;

    public void registrar(WebSocketSession sesion) {
        this.sesion = sesion;
        registrarLatido();
    }

    public void limpiar() {
        this.sesion = null;
    }

    public void registrarLatido() {
        this.ultimoLatido = Instant.now();
    }

    public Instant getUltimoLatido() {
        return ultimoLatido;
    }

    public boolean estaAbierta() {
        WebSocketSession actual = sesion;
        return actual != null && actual.isOpen();
    }

    public void enviar(String json) {
        WebSocketSession actual = sesion;
        if (actual == null || !actual.isOpen()) {
            LOG.warn("No hay dispositivo conectado; se descarta el mensaje {}", json);
            return;
        }
        try {
            synchronized (actual) {
                actual.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            LOG.error("Fallo enviando al dispositivo: {}", e.getMessage());
        }
    }

    public void cerrar() {
        WebSocketSession actual = sesion;
        if (actual == null) {
            return;
        }
        try {
            actual.close();
        } catch (IOException e) {
            LOG.warn("Fallo cerrando la sesion del dispositivo: {}", e.getMessage());
        } finally {
            limpiar();
        }
    }
}
