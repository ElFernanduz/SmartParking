package com.smartparking.service;

import com.smartparking.model.Parqueadero;
import com.smartparking.repository.ParqueaderoRepository;
import org.springframework.stereotype.Service;

@Service
public class ParqueaderoService {

    private static final int CAPACIDAD_MAXIMA_POR_DEFECTO = 20;

    private final ParqueaderoRepository parqueaderoRepository;

    public ParqueaderoService(ParqueaderoRepository parqueaderoRepository) {
        this.parqueaderoRepository = parqueaderoRepository;
    }

    // Devuelve el único registro de parqueadero, creándolo si no existe.
    public Parqueadero obtenerParqueadero() {
        return parqueaderoRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> parqueaderoRepository.save(
                        new Parqueadero(CAPACIDAD_MAXIMA_POR_DEFECTO)));
    }

    public Parqueadero actualizarEspacios(int cantidad) {
        Parqueadero parqueadero = obtenerParqueadero();
        parqueadero.actualizarEspacios(cantidad);
        return parqueaderoRepository.save(parqueadero);
    }

    public int obtenerDisponibilidad() {
        return obtenerParqueadero().obtenerDisponibilidad();
    }
}
