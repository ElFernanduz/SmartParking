package com.tecno.Smartparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Bitacora de lo que ocurre en el sistema. */
@Entity
@Table(name = "evento_sistema")
public class EventoSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "detalle")
    private String detalle;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    protected EventoSistema() {
    }

    public EventoSistema(String tipo, String detalle, LocalDateTime timestamp) {
        this.tipo = tipo;
        this.detalle = detalle;
        this.timestamp = timestamp;
    }

    public static EventoSistema de(String tipo, String detalle) {
        return new EventoSistema(tipo, detalle, LocalDateTime.now());
    }

    public Long getId() {
        return id;
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
