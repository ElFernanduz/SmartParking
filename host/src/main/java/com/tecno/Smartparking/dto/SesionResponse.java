package com.tecno.Smartparking.dto;

import java.util.Set;

/** Lo que el tablero necesita saber de la sesion para pintar sus controles. */
public record SesionResponse(boolean autenticado,
                             String usuario,
                             String email,
                             Set<String> roles,
                             Set<String> permisos) {

    public static SesionResponse anonima() {
        return new SesionResponse(false, null, null, Set.of(), Set.of());
    }
}
