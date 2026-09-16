package com.unillanos.smartparking.dominio.puerto.salida;

import com.unillanos.smartparking.dominio.modelo.FiltroRegistros;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import java.util.List;
import java.util.Optional;

public interface RepositorioRegistroAcceso {

    RegistroAcceso guardar(RegistroAcceso registro);

    int contarActivos();

    /** Para asociar la salida con su ingreso. */
    Optional<RegistroAcceso> buscarActivoMasAntiguo();

    List<RegistroAcceso> buscarTodos(FiltroRegistros filtro);

    /** Descarta una visita que nunca llego a ocurrir (reserva revertida). */
    void eliminar(RegistroAcceso registro);
}
