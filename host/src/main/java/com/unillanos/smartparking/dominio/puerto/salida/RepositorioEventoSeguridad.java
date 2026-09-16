package com.unillanos.smartparking.dominio.puerto.salida;

import com.unillanos.smartparking.dominio.modelo.EventoSeguridad;
import com.unillanos.smartparking.dominio.modelo.FiltroEventos;
import java.util.List;

public interface RepositorioEventoSeguridad {

    EventoSeguridad guardar(EventoSeguridad evento);

    List<EventoSeguridad> buscarTodos(FiltroEventos filtro);
}
