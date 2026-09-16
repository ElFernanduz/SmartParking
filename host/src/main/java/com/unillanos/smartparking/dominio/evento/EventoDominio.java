package com.unillanos.smartparking.dominio.evento;

import java.time.LocalDateTime;

/** Evento del dominio que se publica hacia el tablero. */
public interface EventoDominio {

    LocalDateTime ocurridoEn();
}
