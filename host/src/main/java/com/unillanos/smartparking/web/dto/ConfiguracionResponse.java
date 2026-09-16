package com.unillanos.smartparking.web.dto;

import com.unillanos.smartparking.dominio.modelo.Configuracion;

public record ConfiguracionResponse(int capacidadTotal, int umbralHumo, String estadoSistema) {

    public static ConfiguracionResponse desde(Configuracion configuracion) {
        return new ConfiguracionResponse(configuracion.capacidadTotal(), configuracion.umbralHumo(),
                configuracion.estadoSistema().name());
    }
}
