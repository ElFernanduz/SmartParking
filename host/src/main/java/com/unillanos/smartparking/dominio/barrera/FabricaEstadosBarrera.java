package com.unillanos.smartparking.dominio.barrera;

/** Factory Method: centraliza la creacion de los estados de la barrera. */
public final class FabricaEstadosBarrera {

    private static final EstadoBarrera CERRADA = new EstadoCerrada();
    private static final EstadoBarrera ABRIENDO = new EstadoAbriendo();
    private static final EstadoBarrera ABIERTA = new EstadoAbierta();
    private static final EstadoBarrera CERRANDO = new EstadoCerrando();

    private FabricaEstadosBarrera() {
    }

    public static EstadoBarrera cerrada() {
        return CERRADA;
    }

    public static EstadoBarrera abriendo() {
        return ABRIENDO;
    }

    public static EstadoBarrera abierta() {
        return ABIERTA;
    }

    public static EstadoBarrera cerrando() {
        return CERRANDO;
    }
}
