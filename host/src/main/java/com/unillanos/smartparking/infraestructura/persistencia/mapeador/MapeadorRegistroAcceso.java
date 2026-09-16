package com.unillanos.smartparking.infraestructura.persistencia.mapeador;

import com.unillanos.smartparking.dominio.modelo.EstadoVisita;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.modelo.Vehiculo;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.RegistroAccesoEntity;

public final class MapeadorRegistroAcceso {

    private MapeadorRegistroAcceso() {
    }

    public static RegistroAccesoEntity aEntidad(RegistroAcceso registro) {
        String placa = registro.getVehiculo() == null ? null : registro.getVehiculo().getPlaca();
        return new RegistroAccesoEntity(registro.getId(), placa,
                MapeadorFechas.aTexto(registro.getHoraEntrada()),
                MapeadorFechas.aTexto(registro.getHoraSalida()),
                registro.getEstadoVisita().name());
    }

    /** Del vehiculo solo se conserva la placa; el resto de sus datos no viaja en el historial. */
    public static RegistroAcceso aDominio(RegistroAccesoEntity entidad) {
        Vehiculo vehiculo = entidad.getPlaca() == null
                ? null
                : new Vehiculo(entidad.getPlaca(), null, null);
        return new RegistroAcceso(entidad.getId(), vehiculo,
                MapeadorFechas.aFecha(entidad.getHoraEntrada()),
                MapeadorFechas.aFecha(entidad.getHoraSalida()),
                EstadoVisita.valueOf(entidad.getEstadoVisita()));
    }
}
