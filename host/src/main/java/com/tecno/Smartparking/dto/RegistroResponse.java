package com.tecno.Smartparking.dto;

import com.tecno.Smartparking.model.RegistroAcceso;
import java.time.LocalDateTime;

public record RegistroResponse(Long id,
                               String placa,
                               LocalDateTime horaEntrada,
                               LocalDateTime horaSalida,
                               String estadoVisita,
                               Long duracionMinutos) {

    public static RegistroResponse desde(RegistroAcceso registro) {
        Long duracion = registro.estaActivo() ? null : registro.duracion().toMinutes();
        return new RegistroResponse(registro.getId(), registro.getPlaca(), registro.getHoraEntrada(),
                registro.getHoraSalida(), registro.getEstadoVisita().name(), duracion);
    }
}
