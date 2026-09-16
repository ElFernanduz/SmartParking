package com.unillanos.smartparking.dominio.evento;

import java.time.LocalDateTime;

public record VehiculoIngresadoEvento(Long registroId, LocalDateTime ocurridoEn) implements EventoDominio {

    public static VehiculoIngresadoEvento ahora(Long registroId) {
        return new VehiculoIngresadoEvento(registroId, LocalDateTime.now());
    }
}
