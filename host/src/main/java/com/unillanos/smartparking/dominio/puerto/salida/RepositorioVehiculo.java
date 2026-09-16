package com.unillanos.smartparking.dominio.puerto.salida;

import com.unillanos.smartparking.dominio.modelo.Vehiculo;
import java.util.Optional;

/** Opcional: solo se usa cuando el operador ingresa una placa. */
public interface RepositorioVehiculo {

    Vehiculo guardar(Vehiculo vehiculo);

    Optional<Vehiculo> buscarPorPlaca(String placa);
}
