package com.unillanos.smartparking.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.barrera.Barrera;
import com.unillanos.smartparking.dominio.evento.EstadoSistemaCambiadoEvento;
import com.unillanos.smartparking.dominio.modelo.EstadoAlarma;
import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.modelo.EventoSistema;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.politica.PoliticaAccesoPorDefecto;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioConfiguracion;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EjecutarEmergenciaServicioTest {

    @Mock
    private RepositorioConfiguracion repositorioConfiguracion;
    @Mock
    private RepositorioEventoSistema repositorioEventos;
    @Mock
    private PuertoPasarelaDispositivo pasarela;
    @Mock
    private PuertoPublicadorEventos publicador;

    private EstadoParqueadero estado;
    private EstadoAlarma estadoAlarma;
    private Barrera barreraSalida;
    private EjecutarEmergenciaServicio servicio;

    @BeforeEach
    void prepararServicio() {
        estado = new EstadoParqueadero(4);
        estadoAlarma = new EstadoAlarma();
        barreraSalida = new Barrera(TipoPunto.SALIDA);
        servicio = new EjecutarEmergenciaServicio(estado, estadoAlarma, barreraSalida,
                repositorioConfiguracion, repositorioEventos, new FabricaComandos(pasarela),
                new InvocadorComandos(), publicador);
    }

    @Test
    @DisplayName("La emergencia habilita la salida, bloquea ingresos y sostiene la alarma")
    void laEmergenciaEjecutaElProtocolo() {
        servicio.activarEmergencia();

        assertEquals(EstadoOperativo.EMERGENCIA, estado.getEstado());
        assertTrue(estadoAlarma.estaActiva());
        assertFalse(estado.puedeAdmitir(new PoliticaAccesoPorDefecto()));
        verify(pasarela).abrirBarrera(TipoPunto.SALIDA);
        verify(pasarela).activarAlarma();
        verify(repositorioConfiguracion).guardarEstadoSistema(EstadoOperativo.EMERGENCIA);
        verify(repositorioEventos).guardar(any(EventoSistema.class));
        verify(publicador).publicar(any(EstadoSistemaCambiadoEvento.class));
    }

    @Test
    @DisplayName("Limpiar la emergencia silencia la alarma y restablece la operacion")
    void limpiarRestableceLaOperacion() {
        servicio.activarEmergencia();

        servicio.limpiarEmergencia();

        assertEquals(EstadoOperativo.OPERATIVO, estado.getEstado());
        assertFalse(estadoAlarma.estaActiva());
        assertTrue(estado.puedeAdmitir(new PoliticaAccesoPorDefecto()));
        verify(pasarela).silenciarAlarma();
        verify(pasarela).cerrarBarrera(TipoPunto.SALIDA);
    }

    @Test
    @DisplayName("Activar dos veces la emergencia no repite el protocolo")
    void laActivacionEsIdempotente() {
        servicio.activarEmergencia();
        servicio.activarEmergencia();

        verify(pasarela).activarAlarma();
        verify(pasarela).abrirBarrera(TipoPunto.SALIDA);
    }
}
