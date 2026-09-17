package com.tecno.Smartparking.service.politica;

import com.tecno.Smartparking.model.EstadoOperativo;
import com.tecno.Smartparking.service.EstadoParqueaderoService;
import org.springframework.stereotype.Component;

/** Admite el ingreso si hay cupo y el sistema esta operativo. */
@Component
public class PoliticaAccesoPorDefecto implements PoliticaAcceso {

    @Override
    public boolean admiteIngreso(EstadoParqueaderoService estado) {
        return estado.getCuposDisponibles() > 0 && estado.getEstado() == EstadoOperativo.OPERATIVO;
    }
}
