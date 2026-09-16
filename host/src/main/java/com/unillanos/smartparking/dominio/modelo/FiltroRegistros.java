package com.unillanos.smartparking.dominio.modelo;

import java.time.LocalDateTime;

/** Filtro de consulta del historial de accesos. Los campos nulos no filtran. */
public record FiltroRegistros(LocalDateTime desde,
                              LocalDateTime hasta,
                              EstadoVisita estadoVisita,
                              int limite) {

    public static final int LIMITE_POR_DEFECTO = 100;

    public static FiltroRegistros sinFiltro() {
        return new FiltroRegistros(null, null, null, LIMITE_POR_DEFECTO);
    }
}
