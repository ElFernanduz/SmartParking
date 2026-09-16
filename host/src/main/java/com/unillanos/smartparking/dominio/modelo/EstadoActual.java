package com.unillanos.smartparking.dominio.modelo;

/** Foto del estado del parqueadero para el tablero. */
public record EstadoActual(int cuposDisponibles,
                           int capacidadTotal,
                           EstadoOperativo estado,
                           boolean alarmaActiva,
                           boolean dispositivoConectado) {
}
