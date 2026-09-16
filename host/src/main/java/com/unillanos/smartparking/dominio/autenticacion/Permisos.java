package com.unillanos.smartparking.dominio.autenticacion;

/** Codigos de permiso que gobiernan las acciones de operador del parqueadero. */
public final class Permisos {

    public static final String BARRERA_CONTROL = "BARRERA_CONTROL";
    public static final String EMERGENCIA_CONTROL = "EMERGENCIA_CONTROL";
    public static final String CONFIG_UPDATE = "CONFIG_UPDATE";
    public static final String CUPOS_SYNC = "CUPOS_SYNC";

    private Permisos() {
    }
}
