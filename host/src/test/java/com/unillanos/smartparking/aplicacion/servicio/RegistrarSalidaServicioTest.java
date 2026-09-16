package com.unillanos.smartparking.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unillanos.smartparking.aplicacion.EjecutorSerie;
import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.barrera.Barrera;
import com.unillanos.smartparking.dominio.evento.CuposCambiadosEvento;
import com.unillanos.smartparking.dominio.evento.VehiculoEgresadoEvento;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
import java.time.LocalDateTime;
import java.util.Optional;
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
class RegistrarSalidaServicioTest {

    @Mock
    private PuertoPasarelaDispositivo pasarela;
    @Mock
    private RepositorioRegistroAcceso repositorioRegistros;
    @Mock
    private PuertoPublicadorEventos publicador;

    private EstadoParqueadero estado;
    private Barrera barrera;
    private RegistrarSalidaServicio servicio;

    @BeforeEach
    void prepararServicio() {
        estado = new EstadoParqueadero(3);
        barrera = new Barrera(TipoPunto.SALIDA);
        servicio = new RegistrarSalidaServicio(estado, barrera, repositorioRegistros,
                new FabricaComandos(pasarela), new InvocadorComandos(), publicador,
                new EjecutorSerie(), 60_000L);
    }

    @Test
    @DisplayName("La salida abre la barrera sin condicionar a cupos")
    void laSalidaAbreSiempre() {
        estado.fijarCuposDisponibles(0);

        servicio.alDetectarSalida();

        verify(pasarela).abrirBarrera(TipoPunto.SALIDA);
        assertEquals("ABIERTA", barrera.getEstado().nombre());
    }

    @Test
    @DisplayName("Confirmar la salida suma el cupo y cierra el registro activo mas antiguo")
    void confirmarSalidaSumaCupoYCierraVisita() {
        estado.registrarEntrada();
        RegistroAcceso activo = new RegistroAcceso(7L, null, LocalDateTime.now().minusHours(1),
                null, com.unillanos.smartparking.dominio.modelo.EstadoVisita.ACTIVO);
        when(repositorioRegistros.buscarActivoMasAntiguo()).thenReturn(Optional.of(activo));

        servicio.alDetectarSalida();
        servicio.confirmarSalida();

        assertEquals(3, estado.getCuposDisponibles());
        assertEquals(com.unillanos.smartparking.dominio.modelo.EstadoVisita.FINALIZADO,
                activo.getEstadoVisita());
        verify(repositorioRegistros).guardar(activo);
        verify(pasarela).actualizarPantalla(3);
        verify(pasarela).cerrarBarrera(TipoPunto.SALIDA);
        verify(publicador).publicar(any(VehiculoEgresadoEvento.class));
        verify(publicador).publicar(any(CuposCambiadosEvento.class));
    }

    @Test
    @DisplayName("Una salida sin registro activo no rompe el conteo")
    void salidaSinRegistroActivo() {
        when(repositorioRegistros.buscarActivoMasAntiguo()).thenReturn(Optional.empty());

        servicio.alDetectarSalida();
        servicio.confirmarSalida();

        assertEquals(3, estado.getCuposDisponibles());
        verify(pasarela).cerrarBarrera(TipoPunto.SALIDA);
    }
}
