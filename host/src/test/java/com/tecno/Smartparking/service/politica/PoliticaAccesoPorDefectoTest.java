package com.tecno.Smartparking.service.politica;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tecno.Smartparking.model.EstadoOperativo;
import com.tecno.Smartparking.service.EstadoParqueaderoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoliticaAccesoPorDefectoTest {

    private final PoliticaAcceso politica = new PoliticaAccesoPorDefecto();

    private EstadoParqueaderoService estado;

    @BeforeEach
    void prepararEstado() {
        estado = new EstadoParqueaderoService();
        estado.inicializar(3, 0, EstadoOperativo.OPERATIVO);
    }

    @Test
    @DisplayName("Admite el ingreso con cupos y sistema operativo")
    void admiteConCuposYOperativo() {
        assertTrue(politica.admiteIngreso(estado));
    }

    @Test
    @DisplayName("No admite el ingreso sin cupos")
    void noAdmiteSinCupos() {
        estado.fijarCuposDisponibles(0);

        assertFalse(politica.admiteIngreso(estado));
    }

    @Test
    @DisplayName("No admite el ingreso en emergencia aunque haya cupos")
    void noAdmiteEnEmergencia() {
        estado.cambiarEstado(EstadoOperativo.EMERGENCIA);

        assertFalse(politica.admiteIngreso(estado));
    }
}
