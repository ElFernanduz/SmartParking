package com.unillanos.smartparking.dominio.modelo;

import java.time.LocalDateTime;

/** Filtro de consulta del historial de eventos de seguridad. */
public record FiltroEventos(LocalDateTime desde, LocalDateTime hasta, int limite) {

    public static final int LIMITE_POR_DEFECTO = 100;

    public static FiltroEventos sinFiltro() {
        return new FiltroEventos(null, null, LIMITE_POR_DEFECTO);
    }
}
