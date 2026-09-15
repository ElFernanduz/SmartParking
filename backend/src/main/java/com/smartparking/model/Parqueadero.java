package com.smartparking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Parqueadero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int capacidadMaxima;
    private int espaciosDisponibles;

    protected Parqueadero() {
        // requerido por JPA
    }

    public Parqueadero(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
        this.espaciosDisponibles = capacidadMaxima;
    }

    public void actualizarEspacios(int cantidad) {
        int nuevoValor = this.espaciosDisponibles + cantidad;
        if (nuevoValor < 0) nuevoValor = 0;
        if (nuevoValor > this.capacidadMaxima) nuevoValor = this.capacidadMaxima;
        this.espaciosDisponibles = nuevoValor;
    }

    public int obtenerDisponibilidad() {
        return this.espaciosDisponibles;
    }

    public Long getId() { return id; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public int getEspaciosDisponibles() { return espaciosDisponibles; }
    public void setEspaciosDisponibles(int espaciosDisponibles) { this.espaciosDisponibles = espaciosDisponibles; }
}
