package com.tecno.Smartparking.service.barrera;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tecno.Smartparking.model.TipoPunto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BarreraTest {

    @Test
    @DisplayName("La barrera nace cerrada")
    void naceCerrada() {
        Barrera barrera = new Barrera(TipoPunto.ENTRADA);

        assertTrue(barrera.estaCerrada());
        assertEquals("CERRADA", barrera.getEstado().nombre());
    }

    @Test
    @DisplayName("Ciclo completo: cerrada, abriendo, abierta, cerrando, cerrada")
    void cicloCompleto() {
        Barrera barrera = new Barrera(TipoPunto.ENTRADA);

        barrera.autorizar();
        assertEquals("ABRIENDO", barrera.getEstado().nombre());

        barrera.marcarAbierta();
        assertEquals("ABIERTA", barrera.getEstado().nombre());

        barrera.marcarVehiculoPaso();
        assertEquals("CERRANDO", barrera.getEstado().nombre());

        barrera.marcarCerrada();
        assertEquals("CERRADA", barrera.getEstado().nombre());
    }

    @Test
    @DisplayName("El tiempo de espera con el punto libre cierra la barrera")
    void tiempoDeEsperaCierra() {
        Barrera barrera = new Barrera(TipoPunto.ENTRADA);
        barrera.autorizar();
        barrera.marcarAbierta();

        barrera.marcarTiempoDeEspera();

        assertEquals("CERRANDO", barrera.getEstado().nombre());
    }

    @Test
    @DisplayName("Con el vehiculo aun presente la barrera vuelve a subir en vez de cerrar")
    void nuncaCierraSobreUnVehiculo() {
        Barrera barrera = new Barrera(TipoPunto.SALIDA);
        barrera.autorizar();
        barrera.marcarAbierta();
        barrera.marcarVehiculoPaso();

        barrera.marcarVehiculoPresente();

        assertEquals("ABRIENDO", barrera.getEstado().nombre());
    }

    @Test
    @DisplayName("Los eventos que no corresponden al estado actual se ignoran")
    void losEventosFueraDeLugarSeIgnoran() {
        Barrera barrera = new Barrera(TipoPunto.ENTRADA);

        barrera.marcarAbierta();
        barrera.marcarVehiculoPaso();
        barrera.marcarCerrada();

        assertEquals("CERRADA", barrera.getEstado().nombre());
    }
}
