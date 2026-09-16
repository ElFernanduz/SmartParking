package com.unillanos.smartparking.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehiculo")
public class VehiculoEntity {

    @Id
    @Column(name = "placa")
    private String placa;

    @Column(name = "tipo_vehiculo")
    private String tipoVehiculo;

    @Column(name = "fecha_registro")
    private String fechaRegistro;

    protected VehiculoEntity() {
    }

    public VehiculoEntity(String placa, String tipoVehiculo, String fechaRegistro) {
        this.placa = placa;
        this.tipoVehiculo = tipoVehiculo;
        this.fechaRegistro = fechaRegistro;
    }

    public String getPlaca() {
        return placa;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }
}
