package com.tecno.Smartparking.service.barrera;

public class EstadoAbierta implements EstadoBarrera {

    @Override
    public String nombre() {
        return "ABIERTA";
    }

    @Override
    public EstadoBarrera marcarVehiculoPaso() {
        return FabricaEstadosBarrera.cerrando();
    }

    /** Solo se invoca cuando el punto quedo libre; con el sensor ocupado no se cierra. */
    @Override
    public EstadoBarrera marcarTiempoDeEspera() {
        return FabricaEstadosBarrera.cerrando();
    }
}
