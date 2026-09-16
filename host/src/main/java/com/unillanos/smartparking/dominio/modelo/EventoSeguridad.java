package com.unillanos.smartparking.dominio.modelo;

import java.time.LocalDateTime;

/** Registro de una lectura de humo que supero el umbral configurado. */
public class EventoSeguridad {

    private Long id;
    private final int nivelGasRegistrado;
    private final int umbral;
    private final LocalDateTime timestamp;
    private final boolean requiereEvacuacion;

    public EventoSeguridad(Long id, int nivelGasRegistrado, int umbral,
                           LocalDateTime timestamp, boolean requiereEvacuacion) {
        this.id = id;
        this.nivelGasRegistrado = nivelGasRegistrado;
        this.umbral = umbral;
        this.timestamp = timestamp;
        this.requiereEvacuacion = requiereEvacuacion;
    }

    public Long getId() {
        return id;
    }

    public void asignarId(Long id) {
        this.id = id;
    }

    public int getNivelGasRegistrado() {
        return nivelGasRegistrado;
    }

    public int getUmbral() {
        return umbral;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean requiereEvacuacion() {
        return requiereEvacuacion;
    }
}
