package com.unillanos.smartparking.dominio.politica;

import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;

/** Admite el ingreso si hay cupo y el sistema esta operativo. */
public class PoliticaAccesoPorDefecto implements PoliticaAcceso {

    @Override
    public boolean admiteIngreso(EstadoParqueadero estado) {
        return estado.getCuposDisponibles() > 0 && estado.getEstado() == EstadoOperativo.OPERATIVO;
    }
}
