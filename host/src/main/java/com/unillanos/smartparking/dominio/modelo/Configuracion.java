package com.unillanos.smartparking.dominio.modelo;

/** Configuracion vigente del sistema. */
public record Configuracion(int capacidadTotal, int umbralHumo, EstadoOperativo estadoSistema) {
}
