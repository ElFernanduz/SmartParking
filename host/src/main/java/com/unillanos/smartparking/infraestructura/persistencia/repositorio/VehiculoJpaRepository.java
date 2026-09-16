package com.unillanos.smartparking.infraestructura.persistencia.repositorio;

import com.unillanos.smartparking.infraestructura.persistencia.entidad.VehiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoJpaRepository extends JpaRepository<VehiculoEntity, String> {
}
