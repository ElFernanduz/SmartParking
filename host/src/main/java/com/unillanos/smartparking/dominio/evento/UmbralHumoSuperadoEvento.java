package com.unillanos.smartparking.dominio.evento;

import java.time.LocalDateTime;

public record UmbralHumoSuperadoEvento(int nivel, int umbral, LocalDateTime ocurridoEn) implements EventoDominio {

    public static UmbralHumoSuperadoEvento ahora(int nivel, int umbral) {
        return new UmbralHumoSuperadoEvento(nivel, umbral, LocalDateTime.now());
    }
}
