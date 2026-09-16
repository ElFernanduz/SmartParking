package com.unillanos.smartparking.dominio.evento;

import java.time.LocalDateTime;

public record CuposCambiadosEvento(int cuposDisponibles, int capacidadTotal,
                                   LocalDateTime ocurridoEn) implements EventoDominio {

    public static CuposCambiadosEvento ahora(int cuposDisponibles, int capacidadTotal) {
        return new CuposCambiadosEvento(cuposDisponibles, capacidadTotal, LocalDateTime.now());
    }
}
