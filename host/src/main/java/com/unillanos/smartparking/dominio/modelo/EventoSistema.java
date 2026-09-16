package com.unillanos.smartparking.dominio.modelo;

import java.time.LocalDateTime;

/** Bitacora de lo que ocurre en el sistema. */
public class EventoSistema {

    private Long id;
    private final String tipo;
    private final String detalle;
    private final LocalDateTime timestamp;

    public EventoSistema(Long id, String tipo, String detalle, LocalDateTime timestamp) {
        this.id = id;
        this.tipo = tipo;
        this.detalle = detalle;
        this.timestamp = timestamp;
    }

    public static EventoSistema de(String tipo, String detalle) {
        return new EventoSistema(null, tipo, detalle, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void asignarId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDetalle() {
        return detalle;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
