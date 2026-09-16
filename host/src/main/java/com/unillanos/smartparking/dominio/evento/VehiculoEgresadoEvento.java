package com.unillanos.smartparking.dominio.evento;

import java.time.LocalDateTime;

public record VehiculoEgresadoEvento(Long registroId, LocalDateTime ocurridoEn) implements EventoDominio {

    public static VehiculoEgresadoEvento ahora(Long registroId) {
        return new VehiculoEgresadoEvento(registroId, LocalDateTime.now());
    }
}
