package com.unillanos.smartparking.dominio.puerto.entrada;

import com.unillanos.smartparking.dominio.modelo.EventoSeguridad;
import com.unillanos.smartparking.dominio.modelo.FiltroEventos;
import com.unillanos.smartparking.dominio.modelo.FiltroRegistros;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import java.util.List;

public interface ConsultarHistorialCasoUso {

    List<RegistroAcceso> obtenerRegistros(FiltroRegistros filtro);

    List<EventoSeguridad> obtenerEventosSeguridad(FiltroEventos filtro);
}
