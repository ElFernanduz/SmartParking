package com.unillanos.smartparking.infraestructura.persistencia.mapeador;

import com.unillanos.smartparking.dominio.modelo.EventoSeguridad;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.EventoSeguridadEntity;

public final class MapeadorEventoSeguridad {

    private MapeadorEventoSeguridad() {
    }

    public static EventoSeguridadEntity aEntidad(EventoSeguridad evento) {
        return new EventoSeguridadEntity(evento.getId(), evento.getNivelGasRegistrado(),
                evento.getUmbral(), MapeadorFechas.aTexto(evento.getTimestamp()),
                evento.requiereEvacuacion() ? 1 : 0);
    }

    public static EventoSeguridad aDominio(EventoSeguridadEntity entidad) {
        return new EventoSeguridad(entidad.getId(), entidad.getNivelGas(), entidad.getUmbral(),
                MapeadorFechas.aFecha(entidad.getTimestamp()), entidad.getRequiereEvacuacion() != 0);
    }
}
