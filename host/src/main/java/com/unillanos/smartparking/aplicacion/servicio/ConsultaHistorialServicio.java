package com.unillanos.smartparking.aplicacion.servicio;

import com.unillanos.smartparking.dominio.modelo.EventoSeguridad;
import com.unillanos.smartparking.dominio.modelo.FiltroEventos;
import com.unillanos.smartparking.dominio.modelo.FiltroRegistros;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.puerto.entrada.ConsultarHistorialCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSeguridad;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
import java.util.List;

public class ConsultaHistorialServicio implements ConsultarHistorialCasoUso {

    private final RepositorioRegistroAcceso repositorioRegistros;
    private final RepositorioEventoSeguridad repositorioEventos;

    public ConsultaHistorialServicio(RepositorioRegistroAcceso repositorioRegistros,
                                     RepositorioEventoSeguridad repositorioEventos) {
        this.repositorioRegistros = repositorioRegistros;
        this.repositorioEventos = repositorioEventos;
    }

    @Override
    public List<RegistroAcceso> obtenerRegistros(FiltroRegistros filtro) {
        return repositorioRegistros.buscarTodos(filtro);
    }

    @Override
    public List<EventoSeguridad> obtenerEventosSeguridad(FiltroEventos filtro) {
        return repositorioEventos.buscarTodos(filtro);
    }
}
