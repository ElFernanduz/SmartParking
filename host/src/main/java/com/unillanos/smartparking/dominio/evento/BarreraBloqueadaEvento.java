package com.unillanos.smartparking.dominio.evento;

import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import java.time.LocalDateTime;

/** Aviso al operador: hay un vehiculo detenido y la barrera no puede cerrar. */
public record BarreraBloqueadaEvento(TipoPunto punto, LocalDateTime ocurridoEn) implements EventoDominio {

    public static BarreraBloqueadaEvento ahora(TipoPunto punto) {
        return new BarreraBloqueadaEvento(punto, LocalDateTime.now());
    }
}
