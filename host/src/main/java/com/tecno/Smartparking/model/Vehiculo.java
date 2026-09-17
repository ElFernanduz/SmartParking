package com.tecno.Smartparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Opcional: solo se usa si se ingresa una placa manualmente desde el tablero. */
@Entity
@Table(name = "vehiculo")
public class Vehiculo {

    @Id
    @Column(name = "placa")
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo")
    private TipoVehiculo tipoVehiculo;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    protected Vehiculo() {
    }

    public Vehiculo(String placa, TipoVehiculo tipoVehiculo, LocalDateTime fechaRegistro) {
        this.placa = placa;
        this.tipoVehiculo = tipoVehiculo;
        this.fechaRegistro = fechaRegistro;
    }

    public String getPlaca() {
        return placa;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
