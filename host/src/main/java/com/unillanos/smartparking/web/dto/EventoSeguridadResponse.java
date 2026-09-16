package com.unillanos.smartparking.web.dto;

import com.unillanos.smartparking.dominio.modelo.EventoSeguridad;
import java.time.LocalDateTime;

public record EventoSeguridadResponse(Long id,
                                      int nivelGas,
                                      int umbral,
                                      LocalDateTime timestamp,
                                      boolean requiereEvacuacion) {

    public static EventoSeguridadResponse desde(EventoSeguridad evento) {
        return new EventoSeguridadResponse(evento.getId(), evento.getNivelGasRegistrado(),
                evento.getUmbral(), evento.getTimestamp(), evento.requiereEvacuacion());
    }
}
