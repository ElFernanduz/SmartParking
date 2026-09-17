package com.tecno.Smartparking.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Guarda las fechas como texto ISO-8601 de ancho fijo. El ancho fijo importa:
 * las consultas por rango comparan estas cadenas directamente en SQLite.
 */
@Converter(autoApply = true)
public class ConvertidorFechaIso implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalDateTime fecha) {
        return fecha == null ? null : fecha.format(FORMATO);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String texto) {
        return texto == null || texto.isBlank() ? null : LocalDateTime.parse(texto, FORMATO);
    }
}
