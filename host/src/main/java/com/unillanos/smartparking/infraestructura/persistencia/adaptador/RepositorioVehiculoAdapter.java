package com.unillanos.smartparking.infraestructura.persistencia.adaptador;

import com.unillanos.smartparking.dominio.modelo.Vehiculo;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioVehiculo;
import com.unillanos.smartparking.infraestructura.persistencia.mapeador.MapeadorVehiculo;
import com.unillanos.smartparking.infraestructura.persistencia.repositorio.VehiculoJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RepositorioVehiculoAdapter implements RepositorioVehiculo {

    private final VehiculoJpaRepository repositorio;

    public RepositorioVehiculoAdapter(VehiculoJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public Vehiculo guardar(Vehiculo vehiculo) {
        repositorio.save(MapeadorVehiculo.aEntidad(vehiculo));
        return vehiculo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        return repositorio.findById(placa).map(MapeadorVehiculo::aDominio);
    }
}
