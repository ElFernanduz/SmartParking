package com.tecno.Smartparking.service.barrera;

/**
 * Estado de una barrera. Cada evento devuelve el estado resultante; por
 * defecto el evento se ignora, que es la transicion segura ante una orden
 * que no corresponde al estado actual.
 */
public interface EstadoBarrera {

    String nombre();

    default EstadoBarrera autorizar() {
        return this;
    }

    default EstadoBarrera marcarAbierta() {
        return this;
    }

    default EstadoBarrera marcarVehiculoPaso() {
        return this;
    }

    default EstadoBarrera marcarCerrada() {
        return this;
    }

    default EstadoBarrera marcarVehiculoPresente() {
        return this;
    }

    default EstadoBarrera marcarTiempoDeEspera() {
        return this;
    }
}
