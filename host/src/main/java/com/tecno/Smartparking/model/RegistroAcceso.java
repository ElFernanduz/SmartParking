package com.tecno.Smartparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;

/** Una visita al parqueadero: desde que el vehiculo entra hasta que sale. */
@Entity
@Table(name = "registro_acceso")
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "placa")
    private String placa;

    @Column(name = "hora_entrada", nullable = false)
    private LocalDateTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalDateTime horaSalida;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_visita", nullable = false)
    private EstadoVisita estadoVisita;

    protected RegistroAcceso() {
    }

    public RegistroAcceso(String placa, LocalDateTime horaEntrada) {
        this.placa = placa;
        this.horaEntrada = horaEntrada;
        this.estadoVisita = EstadoVisita.ACTIVO;
    }

    public static RegistroAcceso nuevaVisita(LocalDateTime horaEntrada) {
        return new RegistroAcceso(null, horaEntrada);
    }

    public void finalizar(LocalDateTime cuando) {
        if (cuando.isBefore(horaEntrada)) {
            throw new IllegalArgumentException("La hora de salida no puede ser anterior a la de entrada");
        }
        this.horaSalida = cuando;
        this.estadoVisita = EstadoVisita.FINALIZADO;
    }

    public boolean estaActivo() {
        return estadoVisita == EstadoVisita.ACTIVO;
    }

    public Duration duracion() {
        return Duration.between(horaEntrada, horaSalida != null ? horaSalida : LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalDateTime getHoraSalida() {
        return horaSalida;
    }

    public EstadoVisita getEstadoVisita() {
        return estadoVisita;
    }
}
