package com.unillanos.smartparking.dominio.puerto.entrada;

import com.unillanos.smartparking.dominio.modelo.TipoPunto;

public interface ControlManualCasoUso {

    void abrirBarrera(TipoPunto punto);

    void cerrarBarrera(TipoPunto punto);

    /** El dispositivo reporto que no pudo cerrar porque el punto sigue ocupado. */
    void alBloquearseBarrera(TipoPunto punto);
}
