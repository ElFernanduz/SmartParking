package com.unillanos.smartparking.dominio.barrera;

public class EstadoCerrando implements EstadoBarrera {

    @Override
    public String nombre() {
        return "CERRANDO";
    }

    @Override
    public EstadoBarrera marcarCerrada() {
        return FabricaEstadosBarrera.cerrada();
    }

    /** Condicion segura: si el vehiculo sigue en el punto, la barrera vuelve a subir. */
    @Override
    public EstadoBarrera marcarVehiculoPresente() {
        return FabricaEstadosBarrera.abriendo();
    }
}
