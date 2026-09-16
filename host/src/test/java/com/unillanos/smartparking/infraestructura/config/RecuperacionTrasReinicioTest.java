package com.unillanos.smartparking.infraestructura.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioConfiguracion;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Con visitas activas precargadas, el arranque debe reconstruir el conteo de
 * cupos ocupados desde la base.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/smartparking-recuperacion.db",
        "server.port=0"
})
class RecuperacionTrasReinicioTest {

    @Autowired
    private InicializadorEstado inicializador;
    @Autowired
    private EstadoParqueadero estadoParqueadero;
    @Autowired
    private RepositorioRegistroAcceso repositorioRegistros;
    @Autowired
    private RepositorioConfiguracion repositorioConfiguracion;

    /** La base del archivo sobrevive entre corridas, asi que se parte de cero. */
    @BeforeEach
    void limpiarVisitasActivas() {
        Optional<RegistroAcceso> activo = repositorioRegistros.buscarActivoMasAntiguo();
        while (activo.isPresent()) {
            repositorioRegistros.eliminar(activo.get());
            activo = repositorioRegistros.buscarActivoMasAntiguo();
        }
    }

    @Test
    @DisplayName("El arranque reconstruye el conteo contando las visitas activas")
    void elArranqueReconstruyeElConteo() {
        ApplicationArguments sinArgumentos = new DefaultApplicationArguments();
        repositorioConfiguracion.guardarCapacidad(6);
        repositorioConfiguracion.guardarEstadoSistema(EstadoOperativo.OPERATIVO);
        repositorioRegistros.guardar(RegistroAcceso.nuevaVisita(LocalDateTime.now().minusHours(3)));
        repositorioRegistros.guardar(RegistroAcceso.nuevaVisita(LocalDateTime.now().minusHours(1)));

        inicializador.run(sinArgumentos);

        assertEquals(6, estadoParqueadero.getCapacidadTotal());
        assertEquals(2, estadoParqueadero.getCuposOcupados());
        assertEquals(4, estadoParqueadero.getCuposDisponibles());
        assertEquals(EstadoOperativo.OPERATIVO, estadoParqueadero.getEstado());
    }

    @Test
    @DisplayName("El estado de emergencia guardado sobrevive al reinicio")
    void laEmergenciaSobreviveAlReinicio() {
        repositorioConfiguracion.guardarEstadoSistema(EstadoOperativo.EMERGENCIA);

        inicializador.run(new DefaultApplicationArguments());

        assertEquals(EstadoOperativo.EMERGENCIA, estadoParqueadero.getEstado());
        repositorioConfiguracion.guardarEstadoSistema(EstadoOperativo.OPERATIVO);
    }
}
