package com.unillanos.smartparking.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "evento_sistema")
public class EventoSistemaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "detalle")
    private String detalle;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    protected EventoSistemaEntity() {
    }

    public EventoSistemaEntity(Long id, String tipo, String detalle, String timestamp) {
        this.id = id;
        this.tipo = tipo;
        this.detalle = detalle;
        this.timestamp = timestamp;
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

    public String getTimestamp() {
        return timestamp;
    }
}
