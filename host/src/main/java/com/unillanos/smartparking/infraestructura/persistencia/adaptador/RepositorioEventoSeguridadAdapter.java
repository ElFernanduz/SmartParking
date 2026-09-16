package com.unillanos.smartparking.infraestructura.persistencia.adaptador;

import com.unillanos.smartparking.dominio.modelo.EventoSeguridad;
import com.unillanos.smartparking.dominio.modelo.FiltroEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSeguridad;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.EventoSeguridadEntity;
import com.unillanos.smartparking.infraestructura.persistencia.mapeador.MapeadorEventoSeguridad;
import com.unillanos.smartparking.infraestructura.persistencia.mapeador.MapeadorFechas;
import com.unillanos.smartparking.infraestructura.persistencia.repositorio.EventoSeguridadJpaRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RepositorioEventoSeguridadAdapter implements RepositorioEventoSeguridad {

    private final EventoSeguridadJpaRepository repositorio;

    public RepositorioEventoSeguridadAdapter(EventoSeguridadJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public EventoSeguridad guardar(EventoSeguridad evento) {
        EventoSeguridadEntity guardado = repositorio.save(MapeadorEventoSeguridad.aEntidad(evento));
        evento.asignarId(guardado.getId());
        return evento;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoSeguridad> buscarTodos(FiltroEventos filtro) {
        return repositorio.buscarConFiltro(MapeadorFechas.aTexto(filtro.desde()),
                        MapeadorFechas.aTexto(filtro.hasta()), PageRequest.of(0, filtro.limite()))
                .stream()
                .map(MapeadorEventoSeguridad::aDominio)
                .toList();
    }
}
