package com.tecno.Smartparking.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/** Mantiene las sesiones de los navegadores y difunde mensajes JSON. */
@Component
public class TableroWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TableroWebSocketHandler.class);

    private final Set<WebSocketSession> sesiones = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession sesion) {
        sesiones.add(sesion);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession sesion, CloseStatus estado) {
        sesiones.remove(sesion);
    }

    public void difundir(String json) {
        TextMessage mensaje = new TextMessage(json);
        for (WebSocketSession sesion : sesiones) {
            if (!sesion.isOpen()) {
                sesiones.remove(sesion);
                continue;
            }
            try {
                synchronized (sesion) {
                    sesion.sendMessage(mensaje);
                }
            } catch (IOException e) {
                LOG.warn("Fallo enviando al tablero: {}", e.getMessage());
                sesiones.remove(sesion);
            }
        }
    }

    public int sesionesAbiertas() {
        return sesiones.size();
    }
}
