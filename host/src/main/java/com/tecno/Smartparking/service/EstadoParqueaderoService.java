package com.tecno.Smartparking.service;

import com.tecno.Smartparking.exception.NegocioException;
import com.tecno.Smartparking.model.EstadoOperativo;
import org.springframework.stereotype.Service;

/**
 * Unica fuente de verdad del conteo de cupos y del estado operativo. Los
 * metodos van sincronizados porque el tablero lee el estado mientras el
 * ejecutor de eventos lo modifica.
 */
@Service
public class EstadoParqueaderoService {

    private int capacidadTotal;
    private int cuposDisponibles;
    private EstadoOperativo estado = EstadoOperativo.OPERATIVO;

    public synchronized void inicializar(int capacidadTotal, int cuposOcupados, EstadoOperativo estado) {
        if (capacidadTotal < 0) {
            throw new NegocioException("La capacidad total no puede ser negativa");
        }
        this.capacidadTotal = capacidadTotal;
        this.cuposDisponibles = Math.max(0, capacidadTotal - Math.min(cuposOcupados, capacidadTotal));
        this.estado = estado;
        if (estado != EstadoOperativo.EMERGENCIA) {
            sincronizarEstado();
        }
    }

    public synchronized void registrarEntrada() {
        if (cuposDisponibles <= 0) {
            throw new NegocioException("No hay cupos disponibles en el parqueadero");
        }
        cuposDisponibles--;
        sincronizarEstado();
    }

    public synchronized void registrarSalida() {
        if (cuposDisponibles < capacidadTotal) {
            cuposDisponibles++;
        }
        sincronizarEstado();
    }

    public synchronized void actualizarCapacidad(int nueva) {
        if (nueva < 0) {
            throw new NegocioException("La capacidad total no puede ser negativa");
        }
        int ocupados = Math.min(capacidadTotal - cuposDisponibles, nueva);
        capacidadTotal = nueva;
        cuposDisponibles = nueva - ocupados;
        sincronizarEstado();
    }

    /** Recalibracion manual del conteo. */
    public synchronized void fijarCuposDisponibles(int cupos) {
        if (cupos < 0 || cupos > capacidadTotal) {
            throw new NegocioException("Los cupos disponibles deben estar entre 0 y " + capacidadTotal);
        }
        cuposDisponibles = cupos;
        sincronizarEstado();
    }

    public synchronized void cambiarEstado(EstadoOperativo nuevo) {
        estado = nuevo;
        if (nuevo != EstadoOperativo.EMERGENCIA) {
            sincronizarEstado();
        }
    }

    public synchronized boolean estaLleno() {
        return cuposDisponibles == 0;
    }

    public synchronized int getCapacidadTotal() {
        return capacidadTotal;
    }

    public synchronized int getCuposDisponibles() {
        return cuposDisponibles;
    }

    public synchronized int getCuposOcupados() {
        return capacidadTotal - cuposDisponibles;
    }

    public synchronized EstadoOperativo getEstado() {
        return estado;
    }

    /** En emergencia el estado lo gobierna el protocolo, no el conteo. */
    private void sincronizarEstado() {
        if (estado == EstadoOperativo.EMERGENCIA) {
            return;
        }
        estado = estaLleno() ? EstadoOperativo.LLENO : EstadoOperativo.OPERATIVO;
    }
}
