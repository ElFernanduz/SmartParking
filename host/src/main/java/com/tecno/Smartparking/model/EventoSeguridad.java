package com.tecno.Smartparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Lectura de humo que supero el umbral configurado. */
@Entity
@Table(name = "evento_seguridad")
public class EventoSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nivel_gas", nullable = false)
    private int nivelGas;

    @Column(name = "umbral", nullable = false)
    private int umbral;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /** Booleano guardado como entero, segun la convencion del esquema. */
    @Column(name = "requiere_evacuacion", nullable = false)
    private int requiereEvacuacion;

    protected EventoSeguridad() {
    }

    public EventoSeguridad(int nivelGas, int umbral, LocalDateTime timestamp, boolean requiereEvacuacion) {
        this.nivelGas = nivelGas;
        this.umbral = umbral;
        this.timestamp = timestamp;
        this.requiereEvacuacion = requiereEvacuacion ? 1 : 0;
    }

    public Long getId() {
        return id;
    }

    public int getNivelGas() {
        return nivelGas;
    }

    public int getUmbral() {
        return umbral;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean requiereEvacuacion() {
        return requiereEvacuacion != 0;
    }
}
