package com.unillanos.smartparking.dominio.evento;

import java.time.LocalDateTime;

/** Conexion de la ESP32 con el backend, para reflejarla en el tablero. */
public record DispositivoConexionCambiadaEvento(boolean conectado,
                                                LocalDateTime ocurridoEn) implements EventoDominio {

    public static DispositivoConexionCambiadaEvento ahora(boolean conectado) {
        return new DispositivoConexionCambiadaEvento(conectado, LocalDateTime.now());
    }
}
