package com.unillanos.smartparking.dominio.modelo;

import com.unillanos.smartparking.dominio.excepcion.SinCuposDisponiblesExcepcion;
import com.unillanos.smartparking.dominio.excepcion.ValorInvalidoExcepcion;
import com.unillanos.smartparking.dominio.politica.PoliticaAcceso;

/**
 * Raiz de agregado: unica fuente de verdad del conteo y del estado operativo.
 * Los metodos van sincronizados porque el tablero lee el estado mientras el
 * ejecutor de eventos lo modifica.
 */
public class EstadoParqueadero {

    private int capacidadTotal;
    private int cuposDisponibles;
    private EstadoOperativo estado;

    public EstadoParqueadero(int capacidadTotal, int cuposDisponibles, EstadoOperativo estado) {
        if (capacidadTotal < 0) {
            throw new ValorInvalidoExcepcion("La capacidad total no puede ser negativa");
        }
        if (cuposDisponibles < 0 || cuposDisponibles > capacidadTotal) {
            throw new ValorInvalidoExcepcion("Los cupos disponibles deben estar entre 0 y la capacidad total");
        }
        this.capacidadTotal = capacidadTotal;
        this.cuposDisponibles = cuposDisponibles;
        this.estado = estado;
    }

    public EstadoParqueadero(int capacidadTotal) {
        this(capacidadTotal, capacidadTotal, EstadoOperativo.OPERATIVO);
    }

    public synchronized boolean puedeAdmitir(PoliticaAcceso politica) {
        return politica.admiteIngreso(this);
    }

    public synchronized void registrarEntrada() {
        if (cuposDisponibles <= 0) {
            throw new SinCuposDisponiblesExcepcion();
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
            throw new ValorInvalidoExcepcion("La capacidad total no puede ser negativa");
        }
        int ocupados = Math.min(capacidadTotal - cuposDisponibles, nueva);
        capacidadTotal = nueva;
        cuposDisponibles = nueva - ocupados;
        sincronizarEstado();
    }

    public synchronized void fijarCuposDisponibles(int cupos) {
        if (cupos < 0 || cupos > capacidadTotal) {
            throw new ValorInvalidoExcepcion("Los cupos disponibles deben estar entre 0 y la capacidad total");
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
