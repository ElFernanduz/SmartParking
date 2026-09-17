package com.tecno.Smartparking.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tecno.Smartparking.model.TipoPunto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CodificadorMensajesTest {

    private final CodificadorMensajes codificador = new CodificadorMensajes();

    @Test
    @DisplayName("Decodifica un evento simple del dispositivo")
    void decodificaEventoSimple() {
        MensajeDispositivo mensaje = codificador.decodificar("{\"tipo\":\"ENTRADA_DETECTADA\"}");

        assertEquals(MensajeDispositivo.ENTRADA_DETECTADA, mensaje.tipo());
        assertNull(mensaje.punto());
        assertNull(mensaje.nivel());
    }

    @Test
    @DisplayName("Decodifica el paso completado con su punto")
    void decodificaPasoCompletado() {
        MensajeDispositivo mensaje =
                codificador.decodificar("{\"tipo\":\"PASO_COMPLETADO\",\"punto\":\"SALIDA\"}");

        assertEquals(MensajeDispositivo.PASO_COMPLETADO, mensaje.tipo());
        assertEquals(TipoPunto.SALIDA, mensaje.punto());
    }

    @Test
    @DisplayName("Decodifica la telemetria de humo con su nivel")
    void decodificaTelemetria() {
        MensajeDispositivo mensaje =
                codificador.decodificar("{\"tipo\":\"TELEMETRIA_HUMO\",\"nivel\":512}");

        assertEquals(512, mensaje.nivel());
    }

    @Test
    @DisplayName("Un mensaje sin tipo o ilegible se rechaza")
    void mensajeInvalido() {
        assertThrows(CodificadorMensajes.MensajeInvalidoException.class,
                () -> codificador.decodificar("{\"nivel\":3}"));
        assertThrows(CodificadorMensajes.MensajeInvalidoException.class,
                () -> codificador.decodificar("esto no es json"));
        assertThrows(CodificadorMensajes.MensajeInvalidoException.class,
                () -> codificador.decodificar("{\"tipo\":\"PASO_COMPLETADO\",\"punto\":\"PATIO\"}"));
    }

    @Test
    @DisplayName("Codifica los comandos hacia el dispositivo")
    void codificaComandos() {
        assertEquals("{\"tipo\":\"ABRIR\",\"punto\":\"ENTRADA\"}", codificador.abrir(TipoPunto.ENTRADA));
        assertEquals("{\"tipo\":\"CERRAR\",\"punto\":\"SALIDA\"}", codificador.cerrar(TipoPunto.SALIDA));
        assertEquals("{\"tipo\":\"ALARMA\",\"estado\":\"ON\"}", codificador.alarma(true));
        assertEquals("{\"tipo\":\"ALARMA\",\"estado\":\"OFF\"}", codificador.alarma(false));
        assertEquals("{\"tipo\":\"PANTALLA\",\"cupos\":5}", codificador.pantalla(5));
        assertEquals("{\"tipo\":\"CONFIG_UMBRAL\",\"umbral\":400}", codificador.configurarUmbral(400));
        assertEquals("{\"tipo\":\"PING\"}", codificador.ping());
    }

    @Test
    @DisplayName("Lo codificado se puede volver a decodificar")
    void idaYVuelta() {
        MensajeDispositivo mensaje = codificador.decodificar(codificador.abrir(TipoPunto.ENTRADA));

        assertEquals("ABRIR", mensaje.tipo());
        assertEquals(TipoPunto.ENTRADA, mensaje.punto());
    }
}
