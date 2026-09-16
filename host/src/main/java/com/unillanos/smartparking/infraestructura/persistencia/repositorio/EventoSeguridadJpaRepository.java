package com.unillanos.smartparking.infraestructura.persistencia.repositorio;

import com.unillanos.smartparking.infraestructura.persistencia.entidad.EventoSeguridadEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoSeguridadJpaRepository extends JpaRepository<EventoSeguridadEntity, Long> {

    @Query("""
            select e from EventoSeguridadEntity e
            where (:desde is null or e.timestamp >= :desde)
              and (:hasta is null or e.timestamp <= :hasta)
            order by e.timestamp desc
            """)
    List<EventoSeguridadEntity> buscarConFiltro(@Param("desde") String desde,
                                                @Param("hasta") String hasta,
                                                Pageable pagina);
}
