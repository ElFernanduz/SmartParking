package com.unillanos.smartparking.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unillanos.smartparking.aplicacion.EjecutorSerie;
import java.util.concurrent.ScheduledFuture;
import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.barrera.Barrera;
import com.unillanos.smartparking.dominio.evento.CuposCambiadosEvento;
import com.unillanos.smartparking.dominio.evento.VehiculoIngresadoEvento;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.politica.PoliticaAccesoPorDefecto;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
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
class RegistrarEntradaServicioTest {

    @Mock
    private PuertoPasarelaDispositivo pasarela;
    @Mock
    private RepositorioRegistroAcceso repositorioRegistros;
    @Mock
    private RepositorioEventoSistema repositorioEventos;
    @Mock
    private PuertoPublicadorEventos publicador;

    private EstadoParqueadero estado;
    private Barrera barrera;
    private RegistrarEntradaServicio servicio;

    @BeforeEach
    void prepararServicio() {
        estado = new EstadoParqueadero(3);
        barrera = new Barrera(TipoPunto.ENTRADA);
        when(repositorioRegistros.guardar(any(RegistroAcceso.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio = new RegistrarEntradaServicio(estado, new PoliticaAccesoPorDefecto(), barrera,
                repositorioRegistros, repositorioEventos, new FabricaComandos(pasarela),
                new InvocadorComandos(), publicador, new EjecutorSerie(), 60_000L);
    }

    @Test
    @DisplayName("Al detectar una entrada con cupo: reserva, abre, guarda y publica")
    void entradaConCupo() {
        servicio.alDetectarEntrada();

        assertEquals(2, estado.getCuposDisponibles());
        verify(pasarela).abrirBarrera(TipoPunto.ENTRADA);
        verify(repositorioRegistros).guardar(any(RegistroAcceso.class));
        verify(pasarela).actualizarPantalla(2);
        verify(publicador).publicar(any(VehiculoIngresadoEvento.class));
        verify(publicador).publicar(any(CuposCambiadosEvento.class));
        assertEquals("ABIERTA", barrera.getEstado().nombre());
    }

    @Test
    @DisplayName("Sin cupos no se abre la barrera ni se descuenta nada")
    void entradaSinCupo() {
        estado.fijarCuposDisponibles(0);

        servicio.alDetectarEntrada();

        assertEquals(0, estado.getCuposDisponibles());
        verify(pasarela, never()).abrirBarrera(any());
        verify(repositorioRegistros, never()).guardar(any());
        assertEquals("CERRADA", barrera.getEstado().nombre());
    }

    @Test
    @DisplayName("Confirmar el paso cierra la barrera y conserva el cupo descontado")
    void confirmacionCierraLaBarrera() {
        servicio.alDetectarEntrada();

        servicio.confirmarEntrada();

        assertEquals(2, estado.getCuposDisponibles());
        verify(pasarela).cerrarBarrera(TipoPunto.ENTRADA);
        assertEquals("CERRADA", barrera.getEstado().nombre());
    }

    @Test
    @DisplayName("Una segunda deteccion mientras hay una apertura en curso se ignora")
    void deteccionDuplicadaSeIgnora() {
        servicio.alDetectarEntrada();
        servicio.alDetectarEntrada();

        assertEquals(2, estado.getCuposDisponibles());
        verify(pasarela).abrirBarrera(TipoPunto.ENTRADA);
    }

    @Test
    @DisplayName("Si vence la espera sin paso se cierra la barrera y se devuelve el cupo")
    void vencimientoRevierteLaReserva() {
        EjecutorManual ejecutorManual = new EjecutorManual();
        RegistrarEntradaServicio servicioConEspera = new RegistrarEntradaServicio(estado,
                new PoliticaAccesoPorDefecto(), barrera, repositorioRegistros, repositorioEventos,
                new FabricaComandos(pasarela), new InvocadorComandos(), publicador, ejecutorManual, 10L);

        servicioConEspera.alDetectarEntrada();
        assertEquals(2, estado.getCuposDisponibles());

        ejecutorManual.dispararProgramada();

        assertEquals(3, estado.getCuposDisponibles());
        verify(pasarela).cerrarBarrera(TipoPunto.ENTRADA);
        verify(repositorioRegistros).eliminar(any(RegistroAcceso.class));
        assertEquals("CERRADA", barrera.getEstado().nombre());
    }

    /** Ejecutor de prueba: deja disparar el vencimiento a voluntad. */
    private static final class EjecutorManual extends EjecutorSerie {

        private Runnable programada;

        @Override
        public void ejecutar(Runnable tarea) {
            tarea.run();
        }

        @Override
        public ScheduledFuture<?> programar(Runnable tarea, long milisegundos) {
            this.programada = tarea;
            return null;
        }

        void dispararProgramada() {
            programada.run();
        }
    }
}
