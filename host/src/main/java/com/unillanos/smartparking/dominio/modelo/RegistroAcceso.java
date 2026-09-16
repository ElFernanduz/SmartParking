package com.unillanos.smartparking.dominio.modelo;

import com.unillanos.smartparking.dominio.excepcion.ValorInvalidoExcepcion;
import java.time.Duration;
import java.time.LocalDateTime;

/** Una visita al parqueadero: desde que el vehiculo entra hasta que sale. */
public class RegistroAcceso {

    private Long id;
    private final Vehiculo vehiculo;
    private final LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private EstadoVisita estadoVisita;

    public RegistroAcceso(Long id, Vehiculo vehiculo, LocalDateTime horaEntrada,
                          LocalDateTime horaSalida, EstadoVisita estadoVisita) {
        this.id = id;
        this.vehiculo = vehiculo;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.estadoVisita = estadoVisita;
    }

    public static RegistroAcceso nuevaVisita(LocalDateTime horaEntrada) {
        return new RegistroAcceso(null, null, horaEntrada, null, EstadoVisita.ACTIVO);
    }

    public static RegistroAcceso nuevaVisita(LocalDateTime horaEntrada, Vehiculo vehiculo) {
        return new RegistroAcceso(null, vehiculo, horaEntrada, null, EstadoVisita.ACTIVO);
    }

    public void finalizar(LocalDateTime cuando) {
        if (cuando.isBefore(horaEntrada)) {
            throw new ValorInvalidoExcepcion("La hora de salida no puede ser anterior a la de entrada");
        }
        this.horaSalida = cuando;
        this.estadoVisita = EstadoVisita.FINALIZADO;
    }

    public boolean estaActivo() {
        return estadoVisita == EstadoVisita.ACTIVO;
    }

    public Duration duracion() {
        LocalDateTime fin = horaSalida != null ? horaSalida : LocalDateTime.now();
        return Duration.between(horaEntrada, fin);
    }

    public Long getId() {
        return id;
    }

    public void asignarId(Long id) {
        this.id = id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
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
