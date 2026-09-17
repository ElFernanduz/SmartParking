package com.tecno.Smartparking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tecno.Smartparking.model.EstadoOperativo;
import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.model.RegistroAcceso;
import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.repository.EventoSistemaRepository;
import com.tecno.Smartparking.repository.RegistroAccesoRepository;
import com.tecno.Smartparking.service.barrera.Barrera;
import com.tecno.Smartparking.service.comando.FabricaComandos;
import com.tecno.Smartparking.service.comando.InvocadorComandos;
import com.tecno.Smartparking.service.politica.PoliticaAccesoPorDefecto;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
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
class AccesoServiceTest {

    @Mock
    private DispositivoService dispositivo;
    @Mock
    private RegistroAccesoRepository registros;
    @Mock
    private EventoSistemaRepository eventos;
    @Mock
    private NotificacionService notificaciones;

    private EstadoParqueaderoService estado;
    private Barrera barreraEntrada;
    private Barrera barreraSalida;
    private EjecutorManual ejecutor;
    private AccesoService acceso;

    @BeforeEach
    void prepararServicio() {
        estado = new EstadoParqueaderoService();
        estado.inicializar(3, 0, EstadoOperativo.OPERATIVO);
        barreraEntrada = new Barrera(TipoPunto.ENTRADA);
        barreraSalida = new Barrera(TipoPunto.SALIDA);
        ejecutor = new EjecutorManual();

        when(registros.save(any(RegistroAcceso.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        acceso = new AccesoService(estado, new PoliticaAccesoPorDefecto(), barreraEntrada,
                barreraSalida, registros, eventos, new FabricaComandos(dispositivo),
                new InvocadorComandos(), notificaciones, ejecutor, 10_000L);
    }

    @Test
    @DisplayName("Al detectar una entrada con cupo: reserva, abre, guarda y notifica")
    void entradaConCupo() {
        acceso.alDetectarEntrada();

        assertEquals(2, estado.getCuposDisponibles());
        verify(dispositivo).abrirBarrera(TipoPunto.ENTRADA);
        verify(registros).save(any(RegistroAcceso.class));
        verify(dispositivo).actualizarPantalla(2);
        verify(notificaciones).cuposCambiados(2, 3);
        assertEquals("ABIERTA", barreraEntrada.getEstado().nombre());
    }

    @Test
    @DisplayName("Sin cupos no se abre la barrera ni se descuenta nada")
    void entradaSinCupo() {
        estado.fijarCuposDisponibles(0);

        acceso.alDetectarEntrada();

        assertEquals(0, estado.getCuposDisponibles());
        verify(dispositivo, never()).abrirBarrera(any());
        verify(registros, never()).save(any());
        assertEquals("CERRADA", barreraEntrada.getEstado().nombre());
    }

    @Test
    @DisplayName("Confirmar el paso cierra la barrera y conserva el cupo descontado")
    void confirmacionCierraLaBarrera() {
        acceso.alDetectarEntrada();

        acceso.confirmarEntrada();

        assertEquals(2, estado.getCuposDisponibles());
        verify(dispositivo).cerrarBarrera(TipoPunto.ENTRADA);
        assertEquals("CERRADA", barreraEntrada.getEstado().nombre());
    }

    @Test
    @DisplayName("Una segunda deteccion mientras hay una apertura en curso se ignora")
    void deteccionDuplicadaSeIgnora() {
        acceso.alDetectarEntrada();
        acceso.alDetectarEntrada();

        assertEquals(2, estado.getCuposDisponibles());
        verify(dispositivo).abrirBarrera(TipoPunto.ENTRADA);
    }

    @Test
    @DisplayName("Si vence la espera sin paso se cierra la barrera y se devuelve el cupo")
    void vencimientoRevierteLaReserva() {
        acceso.alDetectarEntrada();
        assertEquals(2, estado.getCuposDisponibles());

        ejecutor.dispararProgramada();

        assertEquals(3, estado.getCuposDisponibles());
        verify(dispositivo).cerrarBarrera(TipoPunto.ENTRADA);
        verify(registros).delete(any(RegistroAcceso.class));
        assertEquals("CERRADA", barreraEntrada.getEstado().nombre());
    }

    @Test
    @DisplayName("La salida abre la barrera sin condicionar a cupos")
    void laSalidaAbreSiempre() {
        estado.fijarCuposDisponibles(0);

        acceso.alDetectarSalida();

        verify(dispositivo).abrirBarrera(TipoPunto.SALIDA);
        assertEquals("ABIERTA", barreraSalida.getEstado().nombre());
    }

    @Test
    @DisplayName("Confirmar la salida suma el cupo y cierra el registro activo mas antiguo")
    void confirmarSalidaSumaCupoYCierraVisita() {
        estado.registrarEntrada();
        RegistroAcceso activo = RegistroAcceso.nuevaVisita(LocalDateTime.now().minusHours(1));
        when(registros.findFirstByEstadoVisitaOrderByHoraEntradaAsc(EstadoVisita.ACTIVO))
                .thenReturn(Optional.of(activo));

        acceso.alDetectarSalida();
        acceso.confirmarSalida();

        assertEquals(3, estado.getCuposDisponibles());
        assertEquals(EstadoVisita.FINALIZADO, activo.getEstadoVisita());
        verify(registros).save(activo);
        verify(dispositivo).actualizarPantalla(3);
        verify(dispositivo).cerrarBarrera(TipoPunto.SALIDA);
    }

    @Test
    @DisplayName("Una salida sin registro activo no rompe el conteo")
    void salidaSinRegistroActivo() {
        when(registros.findFirstByEstadoVisitaOrderByHoraEntradaAsc(EstadoVisita.ACTIVO))
                .thenReturn(Optional.empty());

        acceso.alDetectarSalida();
        acceso.confirmarSalida();

        assertEquals(3, estado.getCuposDisponibles());
        verify(dispositivo).cerrarBarrera(TipoPunto.SALIDA);
    }

    /** Ejecutor de prueba: deja disparar el vencimiento a voluntad. */
    private static final class EjecutorManual extends EjecutorEventos {

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
