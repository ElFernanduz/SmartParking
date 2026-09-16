package com.unillanos.smartparking.dominio.barrera;

public class EstadoCerrada implements EstadoBarrera {

    @Override
    public String nombre() {
        return "CERRADA";
    }

    @Override
    public EstadoBarrera autorizar() {
        return FabricaEstadosBarrera.abriendo();
    }
}
