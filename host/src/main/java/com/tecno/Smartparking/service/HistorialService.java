package com.tecno.Smartparking.service;

import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.model.EventoSeguridad;
import com.tecno.Smartparking.model.RegistroAcceso;
import com.tecno.Smartparking.repository.EventoSeguridadRepository;
import com.tecno.Smartparking.repository.RegistroAccesoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta del historial de accesos y de eventos de seguridad. */
@Service
public class HistorialService {

    private final RegistroAccesoRepository registros;
    private final EventoSeguridadRepository eventos;

    public HistorialService(RegistroAccesoRepository registros, EventoSeguridadRepository eventos) {
        this.registros = registros;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public List<RegistroAcceso> buscarRegistros(LocalDateTime desde, LocalDateTime hasta,
                                                EstadoVisita estadoVisita, int limite) {
        return registros.buscarConFiltro(desde, hasta, estadoVisita, PageRequest.of(0, limite));
    }

    @Transactional(readOnly = true)
    public List<EventoSeguridad> buscarEventosSeguridad(LocalDateTime desde, LocalDateTime hasta, int limite) {
        return eventos.buscarConFiltro(desde, hasta, PageRequest.of(0, limite));
    }
}
