package com.tecno.Smartparking.dto;

public record EstadoResponse(int cuposDisponibles,
                             int capacidadTotal,
                             int cuposOcupados,
                             String estado,
                             boolean alarmaActiva,
                             boolean dispositivoConectado) {
}
