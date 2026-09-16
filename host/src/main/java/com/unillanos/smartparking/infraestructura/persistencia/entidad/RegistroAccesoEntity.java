package com.unillanos.smartparking.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "registro_acceso")
public class RegistroAccesoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "placa")
    private String placa;

    @Column(name = "hora_entrada", nullable = false)
    private String horaEntrada;

    @Column(name = "hora_salida")
    private String horaSalida;

    @Column(name = "estado_visita", nullable = false)
    private String estadoVisita;

    protected RegistroAccesoEntity() {
    }

    public RegistroAccesoEntity(Long id, String placa, String horaEntrada,
                                String horaSalida, String estadoVisita) {
        this.id = id;
        this.placa = placa;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.estadoVisita = estadoVisita;
    }

    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public String getEstadoVisita() {
        return estadoVisita;
    }
}
