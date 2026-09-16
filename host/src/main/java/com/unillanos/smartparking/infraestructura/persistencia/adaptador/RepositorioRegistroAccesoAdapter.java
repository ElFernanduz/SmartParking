package com.unillanos.smartparking.infraestructura.persistencia.adaptador;

import com.unillanos.smartparking.dominio.modelo.EstadoVisita;
import com.unillanos.smartparking.dominio.modelo.FiltroRegistros;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.RegistroAccesoEntity;
import com.unillanos.smartparking.infraestructura.persistencia.mapeador.MapeadorFechas;
import com.unillanos.smartparking.infraestructura.persistencia.mapeador.MapeadorRegistroAcceso;
import com.unillanos.smartparking.infraestructura.persistencia.repositorio.RegistroAccesoJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RepositorioRegistroAccesoAdapter implements RepositorioRegistroAcceso {

    private final RegistroAccesoJpaRepository repositorio;

    public RepositorioRegistroAccesoAdapter(RegistroAccesoJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public RegistroAcceso guardar(RegistroAcceso registro) {
        RegistroAccesoEntity guardado = repositorio.save(MapeadorRegistroAcceso.aEntidad(registro));
        registro.asignarId(guardado.getId());
        return registro;
    }

    @Override
    @Transactional(readOnly = true)
    public int contarActivos() {
        return repositorio.countByEstadoVisita(EstadoVisita.ACTIVO.name());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegistroAcceso> buscarActivoMasAntiguo() {
        return repositorio.findFirstByEstadoVisitaOrderByHoraEntradaAsc(EstadoVisita.ACTIVO.name())
                .map(MapeadorRegistroAcceso::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroAcceso> buscarTodos(FiltroRegistros filtro) {
        String estado = filtro.estadoVisita() == null ? null : filtro.estadoVisita().name();
        return repositorio.buscarConFiltro(MapeadorFechas.aTexto(filtro.desde()),
                        MapeadorFechas.aTexto(filtro.hasta()), estado,
                        PageRequest.of(0, filtro.limite()))
                .stream()
                .map(MapeadorRegistroAcceso::aDominio)
                .toList();
    }

    @Override
    @Transactional
    public void eliminar(RegistroAcceso registro) {
        if (registro.getId() != null) {
            repositorio.deleteById(registro.getId());
        }
    }
}
