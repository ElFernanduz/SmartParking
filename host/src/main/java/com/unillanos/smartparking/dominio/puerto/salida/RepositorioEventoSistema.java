package com.unillanos.smartparking.dominio.puerto.salida;

import com.unillanos.smartparking.dominio.modelo.EventoSistema;

public interface RepositorioEventoSistema {

    EventoSistema guardar(EventoSistema evento);
}
