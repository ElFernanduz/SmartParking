package com.unillanos.smartparking.dominio.excepcion;

public class SinCuposDisponiblesExcepcion extends ExcepcionDominio {

    public SinCuposDisponiblesExcepcion() {
        super("No hay cupos disponibles en el parqueadero");
    }
}
