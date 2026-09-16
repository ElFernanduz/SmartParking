package com.unillanos.smartparking.dominio.barrera;

public class EstadoAbriendo implements EstadoBarrera {

    @Override
    public String nombre() {
        return "ABRIENDO";
    }

    @Override
    public EstadoBarrera marcarAbierta() {
        return FabricaEstadosBarrera.abierta();
    }
}
