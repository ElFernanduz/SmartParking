package com.unillanos.smartparking.infraestructura.dispositivo;

import com.unillanos.smartparking.dominio.modelo.TipoPunto;

/** Mensaje entrante del protocolo. Los campos que no aplican llegan nulos. */
public record MensajeDispositivo(String tipo, TipoPunto punto, Integer nivel) {

    public static final String LISTO = "LISTO";
    public static final String ENTRADA_DETECTADA = "ENTRADA_DETECTADA";
    public static final String SALIDA_DETECTADA = "SALIDA_DETECTADA";
    public static final String PASO_COMPLETADO = "PASO_COMPLETADO";
    public static final String TELEMETRIA_HUMO = "TELEMETRIA_HUMO";
    public static final String LATIDO = "LATIDO";
    public static final String BARRERA_BLOQUEADA = "BARRERA_BLOQUEADA";
}
