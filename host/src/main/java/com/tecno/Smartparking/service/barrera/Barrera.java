package com.tecno.Smartparking.service.barrera;

import com.tecno.Smartparking.model.TipoPunto;

/** Barrera de un punto de acceso. Delega cada evento en su estado actual. */
public class Barrera {

    private final TipoPunto punto;
    private EstadoBarrera estado;

    public Barrera(TipoPunto punto) {
        this.punto = punto;
        this.estado = FabricaEstadosBarrera.cerrada();
    }

    public synchronized void autorizar() {
        estado = estado.autorizar();
    }

    public synchronized void marcarAbierta() {
        estado = estado.marcarAbierta();
    }

    public synchronized void marcarVehiculoPaso() {
        estado = estado.marcarVehiculoPaso();
    }

    public synchronized void marcarCerrada() {
        estado = estado.marcarCerrada();
    }

    public synchronized void marcarVehiculoPresente() {
        estado = estado.marcarVehiculoPresente();
    }

    public synchronized void marcarTiempoDeEspera() {
        estado = estado.marcarTiempoDeEspera();
    }

    public TipoPunto getPunto() {
        return punto;
    }

    public synchronized EstadoBarrera getEstado() {
        return estado;
    }

    public synchronized boolean estaCerrada() {
        return estado == FabricaEstadosBarrera.cerrada();
    }
}
