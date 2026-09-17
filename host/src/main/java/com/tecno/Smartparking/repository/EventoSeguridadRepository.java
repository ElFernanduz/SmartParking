package com.tecno.Smartparking.repository;

import com.tecno.Smartparking.model.EventoSeguridad;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoSeguridadRepository extends JpaRepository<EventoSeguridad, Long> {

    @Query("""
            select e from EventoSeguridad e
            where (:desde is null or e.timestamp >= :desde)
              and (:hasta is null or e.timestamp <= :hasta)
            order by e.timestamp desc
            """)
    List<EventoSeguridad> buscarConFiltro(@Param("desde") LocalDateTime desde,
                                          @Param("hasta") LocalDateTime hasta,
                                          Pageable pagina);
}
