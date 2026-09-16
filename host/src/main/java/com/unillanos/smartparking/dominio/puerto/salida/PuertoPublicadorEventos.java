package com.unillanos.smartparking.dominio.puerto.salida;

import com.unillanos.smartparking.dominio.evento.EventoDominio;

public interface PuertoPublicadorEventos {

    void publicar(EventoDominio evento);
}
