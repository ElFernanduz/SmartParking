package com.unillanos.smartparking.infraestructura.persistencia.repositorio;

import com.unillanos.smartparking.infraestructura.persistencia.entidad.EventoSistemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoSistemaJpaRepository extends JpaRepository<EventoSistemaEntity, Long> {
}
