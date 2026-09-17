package com.tecno.Smartparking.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tecno.Smartparking.model.EstadoOperativo;
import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.model.RegistroAcceso;
import com.tecno.Smartparking.repository.RegistroAccesoRepository;
import com.tecno.Smartparking.repository.UserRepository;
import com.tecno.Smartparking.service.ConfiguracionService;
import com.tecno.Smartparking.service.EstadoParqueaderoService;
import java.time.LocalDateTime;
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
    private EstadoParqueaderoService estado;
    @Autowired
    private RegistroAccesoRepository registros;
    @Autowired
    private ConfiguracionService configuracion;
    @Autowired
    private UserRepository usuarios;

    /** La base del archivo sobrevive entre corridas, asi que se parte de cero. */
    @BeforeEach
    void limpiarVisitasActivas() {
        registros.deleteAll();
    }

    @Test
    @DisplayName("El arranque reconstruye el conteo contando las visitas activas")
    void elArranqueReconstruyeElConteo() {
        ApplicationArguments sinArgumentos = new DefaultApplicationArguments();
        configuracion.actualizarCapacidad(6);
        configuracion.guardarEstadoSistema(EstadoOperativo.OPERATIVO);
        registros.save(RegistroAcceso.nuevaVisita(LocalDateTime.now().minusHours(3)));
        registros.save(RegistroAcceso.nuevaVisita(LocalDateTime.now().minusHours(1)));

        inicializador.run(sinArgumentos);

        assertEquals(6, estado.getCapacidadTotal());
        assertEquals(2, estado.getCuposOcupados());
        assertEquals(4, estado.getCuposDisponibles());
        assertEquals(EstadoOperativo.OPERATIVO, estado.getEstado());
    }

    @Test
    @DisplayName("El estado de emergencia guardado sobrevive al reinicio")
    void laEmergenciaSobreviveAlReinicio() {
        configuracion.guardarEstadoSistema(EstadoOperativo.EMERGENCIA);

        inicializador.run(new DefaultApplicationArguments());

        assertEquals(EstadoOperativo.EMERGENCIA, estado.getEstado());
        configuracion.guardarEstadoSistema(EstadoOperativo.OPERATIVO);
    }

    @Test
    @DisplayName("El administrador inicial queda creado al arrancar")
    void elAdministradorInicialExiste() {
        assertTrue(usuarios.findByUsernameOrEmail("admin").isPresent());
        assertEquals(0, registros.countByEstadoVisita(EstadoVisita.FINALIZADO));
    }
}
