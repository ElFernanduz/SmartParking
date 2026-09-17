package com.tecno.Smartparking.repository;

import com.tecno.Smartparking.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, String> {
}
