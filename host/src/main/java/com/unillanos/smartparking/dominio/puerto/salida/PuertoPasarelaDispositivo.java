package com.unillanos.smartparking.dominio.puerto.salida;

import com.unillanos.smartparking.dominio.modelo.TipoPunto;

/** Salida hacia la ESP32. */
public interface PuertoPasarelaDispositivo {

    void abrirBarrera(TipoPunto punto);

    void cerrarBarrera(TipoPunto punto);

    void activarAlarma();

    void silenciarAlarma();

    void actualizarPantalla(int cuposDisponibles);

    void configurarUmbral(int umbral);

    boolean estaConectado();
}
