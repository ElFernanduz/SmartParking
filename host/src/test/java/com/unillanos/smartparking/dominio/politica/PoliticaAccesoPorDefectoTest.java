package com.unillanos.smartparking.dominio.politica;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoliticaAccesoPorDefectoTest {

    private final PoliticaAcceso politica = new PoliticaAccesoPorDefecto();

    @Test
    @DisplayName("Admite el ingreso con cupos y sistema operativo")
    void admiteConCuposYOperativo() {
        assertTrue(politica.admiteIngreso(new EstadoParqueadero(3)));
    }

    @Test
    @DisplayName("No admite el ingreso sin cupos")
    void noAdmiteSinCupos() {
        EstadoParqueadero estado = new EstadoParqueadero(1);
        estado.registrarEntrada();

        assertFalse(politica.admiteIngreso(estado));
    }

    @Test
    @DisplayName("No admite el ingreso en emergencia aunque haya cupos")
    void noAdmiteEnEmergencia() {
        EstadoParqueadero estado = new EstadoParqueadero(5);
        estado.cambiarEstado(EstadoOperativo.EMERGENCIA);

        assertFalse(politica.admiteIngreso(estado));
    }
}
