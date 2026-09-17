package com.tecno.Smartparking.repository;

import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.model.RegistroAcceso;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Long> {

    int countByEstadoVisita(EstadoVisita estadoVisita);

    /** Para emparejar una salida con su ingreso. */
    Optional<RegistroAcceso> findFirstByEstadoVisitaOrderByHoraEntradaAsc(EstadoVisita estadoVisita);

    @Query("""
            select r from RegistroAcceso r
            where (:desde is null or r.horaEntrada >= :desde)
              and (:hasta is null or r.horaEntrada <= :hasta)
              and (:estadoVisita is null or r.estadoVisita = :estadoVisita)
            order by r.horaEntrada desc
            """)
    List<RegistroAcceso> buscarConFiltro(@Param("desde") LocalDateTime desde,
                                         @Param("hasta") LocalDateTime hasta,
                                         @Param("estadoVisita") EstadoVisita estadoVisita,
                                         Pageable pagina);
}
