package com.tecno.Smartparking.service.barrera;

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
