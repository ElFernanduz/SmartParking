package com.unillanos.smartparking.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.evento.CuposCambiadosEvento;
import com.unillanos.smartparking.dominio.excepcion.ValorInvalidoExcepcion;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.modelo.EstadoVisita;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
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
class SincronizarCuposServicioTest {

    @Mock
    private RepositorioRegistroAcceso repositorioRegistros;
    @Mock
    private RepositorioEventoSistema repositorioEventos;
    @Mock
    private PuertoPasarelaDispositivo pasarela;
    @Mock
    private PuertoPublicadorEventos publicador;

    private EstadoParqueadero estado;
    private SincronizarCuposServicio servicio;

    @BeforeEach
    void prepararServicio() {
        estado = new EstadoParqueadero(6);
        servicio = new SincronizarCuposServicio(estado, repositorioRegistros, repositorioEventos,
                new FabricaComandos(pasarela), new InvocadorComandos(), publicador);
    }

    @Test
    @DisplayName("Recalibrar hacia arriba crea las visitas activas que faltan")
    void recalibrarHaciaArriba() {
        when(repositorioRegistros.contarActivos()).thenReturn(0);

        servicio.fijarCuposOcupados(3);

        assertEquals(3, estado.getCuposDisponibles());
        verify(repositorioRegistros, times(3)).guardar(any(RegistroAcceso.class));
        verify(pasarela).actualizarPantalla(3);
        verify(publicador).publicar(any(CuposCambiadosEvento.class));
    }

    @Test
    @DisplayName("Recalibrar hacia abajo cierra las visitas activas sobrantes")
    void recalibrarHaciaAbajo() {
        RegistroAcceso activo = new RegistroAcceso(1L, null, LocalDateTime.now().minusHours(2),
                null, EstadoVisita.ACTIVO);
        when(repositorioRegistros.contarActivos()).thenReturn(1);
        when(repositorioRegistros.buscarActivoMasAntiguo()).thenReturn(Optional.of(activo));

        servicio.fijarCuposOcupados(0);

        assertEquals(6, estado.getCuposDisponibles());
        assertEquals(EstadoVisita.FINALIZADO, activo.getEstadoVisita());
        verify(repositorioRegistros).guardar(activo);
    }

    @Test
    @DisplayName("No se aceptan mas ocupados que la capacidad")
    void ocupadosFueraDeRango() {
        assertThrows(ValorInvalidoExcepcion.class, () -> servicio.fijarCuposOcupados(7));
        assertThrows(ValorInvalidoExcepcion.class, () -> servicio.fijarCuposOcupados(-1));
    }
}
