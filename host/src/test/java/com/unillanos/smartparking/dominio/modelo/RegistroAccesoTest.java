package com.unillanos.smartparking.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unillanos.smartparking.dominio.excepcion.ValorInvalidoExcepcion;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegistroAccesoTest {

    private static final LocalDateTime ENTRADA = LocalDateTime.of(2026, 3, 1, 8, 0);

    @Test
    @DisplayName("Una visita nueva nace activa y sin hora de salida")
    void visitaNuevaEstaActiva() {
        RegistroAcceso registro = RegistroAcceso.nuevaVisita(ENTRADA);

        assertTrue(registro.estaActivo());
        assertEquals(EstadoVisita.ACTIVO, registro.getEstadoVisita());
        assertNull(registro.getHoraSalida());
    }

    @Test
    @DisplayName("Finalizar cierra la visita y calcula la duracion")
    void finalizarCierraLaVisita() {
        RegistroAcceso registro = RegistroAcceso.nuevaVisita(ENTRADA);

        registro.finalizar(ENTRADA.plusMinutes(90));

        assertFalse(registro.estaActivo());
        assertEquals(EstadoVisita.FINALIZADO, registro.getEstadoVisita());
        assertEquals(90, registro.duracion().toMinutes());
    }

    @Test
    @DisplayName("No se puede finalizar antes de la hora de entrada")
    void noSePuedeFinalizarAntesDeEntrar() {
        RegistroAcceso registro = RegistroAcceso.nuevaVisita(ENTRADA);

        assertThrows(ValorInvalidoExcepcion.class, () -> registro.finalizar(ENTRADA.minusMinutes(1)));
    }

    @Test
    @DisplayName("La visita puede llevar un vehiculo asociado")
    void laVisitaPuedeLlevarVehiculo() {
        Vehiculo vehiculo = new Vehiculo("ABC123", TipoVehiculo.CARRO, ENTRADA);

        RegistroAcceso registro = RegistroAcceso.nuevaVisita(ENTRADA, vehiculo);

        assertEquals("ABC123", registro.getVehiculo().getPlaca());
    }
}
