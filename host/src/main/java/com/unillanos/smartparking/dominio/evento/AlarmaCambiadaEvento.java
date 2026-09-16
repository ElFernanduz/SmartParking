package com.unillanos.smartparking.dominio.evento;

import java.time.LocalDateTime;

public record AlarmaCambiadaEvento(boolean activa, LocalDateTime ocurridoEn) implements EventoDominio {

    public static AlarmaCambiadaEvento ahora(boolean activa) {
        return new AlarmaCambiadaEvento(activa, LocalDateTime.now());
    }
}
