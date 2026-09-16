package com.unillanos.smartparking.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "evento_seguridad")
public class EventoSeguridadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nivel_gas", nullable = false)
    private int nivelGas;

    @Column(name = "umbral", nullable = false)
    private int umbral;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    /** Booleano guardado como entero, segun el esquema. */
    @Column(name = "requiere_evacuacion", nullable = false)
    private int requiereEvacuacion;

    protected EventoSeguridadEntity() {
    }

    public EventoSeguridadEntity(Long id, int nivelGas, int umbral,
                                 String timestamp, int requiereEvacuacion) {
        this.id = id;
        this.nivelGas = nivelGas;
        this.umbral = umbral;
        this.timestamp = timestamp;
        this.requiereEvacuacion = requiereEvacuacion;
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

    public String getTimestamp() {
        return timestamp;
    }

    public int getRequiereEvacuacion() {
        return requiereEvacuacion;
    }
}
