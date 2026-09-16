package com.unillanos.smartparking.aplicacion.servicio;

import com.unillanos.smartparking.dominio.modelo.EstadoActual;
import com.unillanos.smartparking.dominio.modelo.EstadoAlarma;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.puerto.entrada.ConsultarEstadoCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;

public class ConsultaEstadoServicio implements ConsultarEstadoCasoUso {

    private final EstadoParqueadero estadoParqueadero;
    private final EstadoAlarma estadoAlarma;
    private final PuertoPasarelaDispositivo pasarela;

    public ConsultaEstadoServicio(EstadoParqueadero estadoParqueadero,
                                  EstadoAlarma estadoAlarma,
                                  PuertoPasarelaDispositivo pasarela) {
        this.estadoParqueadero = estadoParqueadero;
        this.estadoAlarma = estadoAlarma;
        this.pasarela = pasarela;
    }

    @Override
    public EstadoActual obtenerEstado() {
        return new EstadoActual(estadoParqueadero.getCuposDisponibles(),
                estadoParqueadero.getCapacidadTotal(),
                estadoParqueadero.getEstado(),
                estadoAlarma.estaActiva(),
                pasarela.estaConectado());
    }
}
