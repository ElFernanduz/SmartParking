package com.tecno.Smartparking.config;

import com.tecno.Smartparking.websocket.DispositivoWebSocketHandler;
import com.tecno.Smartparking.websocket.TableroWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/** Dos rutas distintas: /dispositivo para la ESP32 y /tablero para los navegadores. */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DispositivoWebSocketHandler dispositivo;
    private final TableroWebSocketHandler tablero;

    public WebSocketConfig(DispositivoWebSocketHandler dispositivo, TableroWebSocketHandler tablero) {
        this.dispositivo = dispositivo;
        this.tablero = tablero;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registro) {
        registro.addHandler(dispositivo, "/dispositivo").setAllowedOriginPatterns("*");
        registro.addHandler(tablero, "/tablero").setAllowedOriginPatterns("*");
    }
}
