package com.unillanos.smartparking.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.evento.UmbralHumoSuperadoEvento;
import com.unillanos.smartparking.dominio.modelo.EstadoAlarma;
import com.unillanos.smartparking.dominio.modelo.EventoSeguridad;
import com.unillanos.smartparking.dominio.puerto.entrada.EjecutarEmergenciaCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioConfiguracion;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSeguridad;
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
class ProcesarLecturaHumoServicioTest {

    @Mock
    private RepositorioConfiguracion repositorioConfiguracion;
    @Mock
    private RepositorioEventoSeguridad repositorioEventos;
    @Mock
    private EjecutarEmergenciaCasoUso emergencia;
    @Mock
    private PuertoPasarelaDispositivo pasarela;
    @Mock
    private PuertoPublicadorEventos publicador;

    private EstadoAlarma estadoAlarma;
    private ProcesarLecturaHumoServicio servicio;

    @BeforeEach
    void prepararServicio() {
        estadoAlarma = new EstadoAlarma();
        when(repositorioConfiguracion.obtenerUmbral()).thenReturn(400);
        servicio = new ProcesarLecturaHumoServicio(repositorioConfiguracion, repositorioEventos,
                emergencia, estadoAlarma, new FabricaComandos(pasarela), new InvocadorComandos(),
                publicador);
    }

    @Test
    @DisplayName("Superar el umbral activa la alarma, registra el evento y dispara la emergencia")
    void superarElUmbralDisparaTodo() {
        servicio.procesarLectura(950);

        verify(pasarela).activarAlarma();
        verify(repositorioEventos).guardar(any(EventoSeguridad.class));
        verify(publicador).publicar(any(UmbralHumoSuperadoEvento.class));
        verify(emergencia).activarEmergencia();
        assertTrue(estadoAlarma.estaActiva());
    }

    @Test
    @DisplayName("Una lectura bajo el umbral no hace nada")
    void lecturaNormalNoHaceNada() {
        servicio.procesarLectura(120);

        verify(pasarela, never()).activarAlarma();
        verify(repositorioEventos, never()).guardar(any());
        verify(emergencia, never()).activarEmergencia();
    }

    @Test
    @DisplayName("Con la alarma ya sonando no se repite el registro en cada lectura")
    void noSeRepiteElRegistroDuranteLaEmergencia() {
        servicio.procesarLectura(950);
        servicio.procesarLectura(980);
        servicio.procesarLectura(1200);

        verify(repositorioEventos).guardar(any(EventoSeguridad.class));
        verify(emergencia).activarEmergencia();
    }
}
