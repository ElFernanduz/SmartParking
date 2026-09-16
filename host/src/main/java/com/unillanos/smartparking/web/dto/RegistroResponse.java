package com.unillanos.smartparking.web.dto;

import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import java.time.LocalDateTime;

public record RegistroResponse(Long id,
                               String placa,
                               LocalDateTime horaEntrada,
                               LocalDateTime horaSalida,
                               String estadoVisita,
                               Long duracionMinutos) {

    public static RegistroResponse desde(RegistroAcceso registro) {
        String placa = registro.getVehiculo() == null ? null : registro.getVehiculo().getPlaca();
        Long duracion = registro.estaActivo() ? null : registro.duracion().toMinutes();
        return new RegistroResponse(registro.getId(), placa, registro.getHoraEntrada(),
                registro.getHoraSalida(), registro.getEstadoVisita().name(), duracion);
    }
}
