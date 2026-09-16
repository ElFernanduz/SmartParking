package com.unillanos.smartparking.infraestructura.dispositivo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketDispositivoConfig implements WebSocketConfigurer {

    private final ManejadorDispositivoWebSocket manejador;

    public WebSocketDispositivoConfig(ManejadorDispositivoWebSocket manejador) {
        this.manejador = manejador;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registro) {
        registro.addHandler(manejador, "/dispositivo").setAllowedOriginPatterns("*");
    }
}
