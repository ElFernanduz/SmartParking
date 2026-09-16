package com.unillanos.smartparking.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/** Opcional: solo se usa si se ingresa una placa manualmente desde el dashboard. */
public class Vehiculo {

    private final String placa;
    private final TipoVehiculo tipoVehiculo;
    private final LocalDateTime fechaRegistro;

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

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro instanceof Vehiculo vehiculo) {
            return Objects.equals(placa, vehiculo.placa);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(placa);
    }
}
