package com.unillanos.smartparking.infraestructura.persistencia.repositorio;

import com.unillanos.smartparking.infraestructura.persistencia.entidad.RegistroAccesoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistroAccesoJpaRepository extends JpaRepository<RegistroAccesoEntity, Long> {

    int countByEstadoVisita(String estadoVisita);

    Optional<RegistroAccesoEntity> findFirstByEstadoVisitaOrderByHoraEntradaAsc(String estadoVisita);

    @Query("""
            select r from RegistroAccesoEntity r
            where (:desde is null or r.horaEntrada >= :desde)
              and (:hasta is null or r.horaEntrada <= :hasta)
              and (:estadoVisita is null or r.estadoVisita = :estadoVisita)
            order by r.horaEntrada desc
            """)
    List<RegistroAccesoEntity> buscarConFiltro(@Param("desde") String desde,
                                               @Param("hasta") String hasta,
                                               @Param("estadoVisita") String estadoVisita,
                                               Pageable pagina);
}
