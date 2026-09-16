package com.unillanos.smartparking.dominio.evento;

import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import java.time.LocalDateTime;

public record EstadoSistemaCambiadoEvento(EstadoOperativo estado, LocalDateTime ocurridoEn) implements EventoDominio {

    public static EstadoSistemaCambiadoEvento ahora(EstadoOperativo estado) {
        return new EstadoSistemaCambiadoEvento(estado, LocalDateTime.now());
    }
}
