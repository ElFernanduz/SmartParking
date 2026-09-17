package com.tecno.Smartparking.dto;

import com.tecno.Smartparking.model.EventoSeguridad;
import java.time.LocalDateTime;

public record EventoSeguridadResponse(Long id,
                                      int nivelGas,
                                      int umbral,
                                      LocalDateTime timestamp,
                                      boolean requiereEvacuacion) {

    public static EventoSeguridadResponse desde(EventoSeguridad evento) {
        return new EventoSeguridadResponse(evento.getId(), evento.getNivelGas(), evento.getUmbral(),
                evento.getTimestamp(), evento.requiereEvacuacion());
    }
}
