package com.unillanos.smartparking.infraestructura.persistencia.repositorio;

import com.unillanos.smartparking.infraestructura.persistencia.entidad.ConfiguracionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionJpaRepository extends JpaRepository<ConfiguracionEntity, String> {
}
