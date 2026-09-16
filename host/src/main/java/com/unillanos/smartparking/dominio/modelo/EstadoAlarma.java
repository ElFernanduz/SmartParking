package com.unillanos.smartparking.dominio.modelo;

/** Estado de la alarma sonora, consultado por el tablero. */
public class EstadoAlarma {

    private boolean activa;

    public synchronized void activar() {
        activa = true;
    }

    public synchronized void silenciar() {
        activa = false;
    }

    public synchronized boolean estaActiva() {
        return activa;
    }
}
