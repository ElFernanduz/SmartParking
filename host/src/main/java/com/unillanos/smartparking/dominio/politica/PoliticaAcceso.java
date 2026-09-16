package com.unillanos.smartparking.dominio.politica;

import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;

/** Estrategia de admision de ingresos. */
public interface PoliticaAcceso {

    boolean admiteIngreso(EstadoParqueadero estado);
}
