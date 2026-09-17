package com.tecno.Smartparking.service.politica;

import com.tecno.Smartparking.service.EstadoParqueaderoService;

/** Estrategia de admision de ingresos. */
public interface PoliticaAcceso {

    boolean admiteIngreso(EstadoParqueaderoService estado);
}
