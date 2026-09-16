package com.unillanos.smartparking.infraestructura.persistencia.mapeador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Fechas en ISO-8601 de ancho fijo. El ancho fijo importa: las consultas por
 * rango comparan estas cadenas directamente en SQLite.
 */
public final class MapeadorFechas {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private MapeadorFechas() {
    }

    public static String aTexto(LocalDateTime fecha) {
        return fecha == null ? null : fecha.format(FORMATO);
    }

    public static LocalDateTime aFecha(String texto) {
        return texto == null || texto.isBlank() ? null : LocalDateTime.parse(texto, FORMATO);
    }
}
