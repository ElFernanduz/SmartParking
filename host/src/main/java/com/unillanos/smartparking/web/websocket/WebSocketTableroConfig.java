package com.unillanos.smartparking.web.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketTableroConfig implements WebSocketConfigurer {

    private final ManejadorTableroWebSocket manejador;

    public WebSocketTableroConfig(ManejadorTableroWebSocket manejador) {
        this.manejador = manejador;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registro) {
        registro.addHandler(manejador, "/tablero").setAllowedOriginPatterns("*");
    }
}
