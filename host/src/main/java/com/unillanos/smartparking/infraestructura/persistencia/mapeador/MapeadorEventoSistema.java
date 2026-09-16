package com.unillanos.smartparking.infraestructura.persistencia.mapeador;

import com.unillanos.smartparking.dominio.modelo.EventoSistema;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.EventoSistemaEntity;

public final class MapeadorEventoSistema {

    private MapeadorEventoSistema() {
    }

    public static EventoSistemaEntity aEntidad(EventoSistema evento) {
        return new EventoSistemaEntity(evento.getId(), evento.getTipo(), evento.getDetalle(),
                MapeadorFechas.aTexto(evento.getTimestamp()));
    }

    public static EventoSistema aDominio(EventoSistemaEntity entidad) {
        return new EventoSistema(entidad.getId(), entidad.getTipo(), entidad.getDetalle(),
                MapeadorFechas.aFecha(entidad.getTimestamp()));
    }
}
