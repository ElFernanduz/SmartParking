package com.unillanos.smartparking.web.dto;

import com.unillanos.smartparking.dominio.modelo.EstadoActual;

public record EstadoResponse(int cuposDisponibles,
                             int capacidadTotal,
                             int cuposOcupados,
                             String estado,
                             boolean alarmaActiva,
                             boolean dispositivoConectado) {

    public static EstadoResponse desde(EstadoActual estado) {
        return new EstadoResponse(estado.cuposDisponibles(), estado.capacidadTotal(),
                estado.capacidadTotal() - estado.cuposDisponibles(), estado.estado().name(),
                estado.alarmaActiva(), estado.dispositivoConectado());
    }
}
