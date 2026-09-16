package com.unillanos.smartparking.infraestructura.persistencia.adaptador;

import com.unillanos.smartparking.dominio.modelo.EventoSistema;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.EventoSistemaEntity;
import com.unillanos.smartparking.infraestructura.persistencia.mapeador.MapeadorEventoSistema;
import com.unillanos.smartparking.infraestructura.persistencia.repositorio.EventoSistemaJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RepositorioEventoSistemaAdapter implements RepositorioEventoSistema {

    private final EventoSistemaJpaRepository repositorio;

    public RepositorioEventoSistemaAdapter(EventoSistemaJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public EventoSistema guardar(EventoSistema evento) {
        EventoSistemaEntity guardado = repositorio.save(MapeadorEventoSistema.aEntidad(evento));
        evento.asignarId(guardado.getId());
        return evento;
    }
}
