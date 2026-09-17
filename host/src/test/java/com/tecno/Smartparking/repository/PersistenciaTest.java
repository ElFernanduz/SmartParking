package com.tecno.Smartparking.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tecno.Smartparking.model.Credential;
import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.model.EventoSeguridad;
import com.tecno.Smartparking.model.EventoSistema;
import com.tecno.Smartparking.model.Permission;
import com.tecno.Smartparking.model.Role;
import com.tecno.Smartparking.model.RegistroAcceso;
import com.tecno.Smartparking.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/smartparking-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:db/schema.sql"
})
class PersistenciaTest {

    @Autowired
    private RegistroAccesoRepository registros;
    @Autowired
    private EventoSeguridadRepository eventosSeguridad;
    @Autowired
    private EventoSistemaRepository eventosSistema;
    @Autowired
    private ParametroRepository parametros;
    @Autowired
    private UserRepository usuarios;

    @Test
    @DisplayName("Guardar una visita le asigna identificador y la deja contada como activa")
    void guardarVisitaActiva() {
        RegistroAcceso registro = registros.save(RegistroAcceso.nuevaVisita(LocalDateTime.now()));

        assertNotNull(registro.getId());
        assertEquals(1, registros.countByEstadoVisita(EstadoVisita.ACTIVO));
    }

    @Test
    @DisplayName("La salida se empareja con el ingreso activo mas antiguo")
    void elActivoMasAntiguoEsElPrimero() {
        LocalDateTime base = LocalDateTime.of(2026, 3, 1, 7, 0);
        registros.save(RegistroAcceso.nuevaVisita(base.plusHours(2)));
        registros.save(RegistroAcceso.nuevaVisita(base));

        Optional<RegistroAcceso> masAntiguo =
                registros.findFirstByEstadoVisitaOrderByHoraEntradaAsc(EstadoVisita.ACTIVO);

        assertTrue(masAntiguo.isPresent());
        assertEquals(base, masAntiguo.get().getHoraEntrada());
    }

    @Test
    @DisplayName("Finalizar una visita la saca del conteo y calcula su duracion")
    void finalizarVisita() {
        LocalDateTime entrada = LocalDateTime.of(2026, 3, 1, 9, 0);
        RegistroAcceso registro = registros.save(RegistroAcceso.nuevaVisita(entrada));

        registro.finalizar(entrada.plusMinutes(45));
        registros.save(registro);

        assertEquals(0, registros.countByEstadoVisita(EstadoVisita.ACTIVO));
        List<RegistroAcceso> finalizados = registros.buscarConFiltro(null, null,
                EstadoVisita.FINALIZADO, PageRequest.of(0, 10));
        assertEquals(1, finalizados.size());
        assertEquals(45, finalizados.get(0).duracion().toMinutes());
    }

    @Test
    @DisplayName("El filtro por rango de fechas acota el historial")
    void filtroPorFechas() {
        LocalDateTime base = LocalDateTime.of(2026, 3, 1, 10, 0);
        registros.save(RegistroAcceso.nuevaVisita(base.minusDays(5)));
        registros.save(RegistroAcceso.nuevaVisita(base));

        List<RegistroAcceso> recientes = registros.buscarConFiltro(base.minusDays(1),
                base.plusDays(1), null, PageRequest.of(0, 10));

        assertEquals(1, recientes.size());
        assertEquals(base, recientes.get(0).getHoraEntrada());
    }

    @Test
    @DisplayName("Un evento de seguridad conserva nivel, umbral y bandera de evacuacion")
    void eventoDeSeguridad() {
        LocalDateTime cuando = LocalDateTime.of(2026, 3, 1, 11, 30);
        eventosSeguridad.save(new EventoSeguridad(850, 400, cuando, true));

        List<EventoSeguridad> encontrados =
                eventosSeguridad.buscarConFiltro(null, null, PageRequest.of(0, 10));

        assertEquals(1, encontrados.size());
        assertEquals(850, encontrados.get(0).getNivelGas());
        assertTrue(encontrados.get(0).requiereEvacuacion());
        assertEquals(cuando, encontrados.get(0).getTimestamp());
    }

    @Test
    @DisplayName("La bitacora del sistema recibe identificador al guardarse")
    void bitacoraDelSistema() {
        EventoSistema evento = eventosSistema.save(EventoSistema.de("PRUEBA", "detalle"));

        assertNotNull(evento.getId());
    }

    @Test
    @DisplayName("La configuracion arranca con los valores del esquema")
    void configuracionPorDefecto() {
        assertEquals("6", parametros.findById("capacidad_total").orElseThrow().getValor());
        assertEquals("400", parametros.findById("umbral_humo").orElseThrow().getValor());
        assertEquals("OPERATIVO", parametros.findById("estado_sistema").orElseThrow().getValor());
    }

    @Test
    @DisplayName("Un usuario se recupera por nombre o por correo, con sus roles y permisos")
    void usuarioConRolesYPermisos() {
        Role rol = new Role("OPERADOR");
        rol.addPermission(new Permission(Permission.BARRERA_CONTROL));
        User user = new User("carlos", "carlos@unillanos.edu.co", new Credential("hash", "sal"));
        user.addRole(rol);
        usuarios.save(user);

        User porNombre = usuarios.findByUsernameOrEmail("carlos").orElseThrow();
        User porCorreo = usuarios.findByUsernameOrEmail("carlos@unillanos.edu.co").orElseThrow();

        assertEquals(porNombre.getId(), porCorreo.getId());
        assertTrue(porNombre.isActive());
        assertEquals("hash", porNombre.getCredential().getPasswordHash());
        assertEquals(1, porNombre.getRoles().size());
        assertTrue(porNombre.getRoles().iterator().next().hasPermission(Permission.BARRERA_CONTROL));
    }

    @Test
    @DisplayName("Desactivar un usuario se refleja al volver a leerlo")
    void ladesactivacionSePersiste() {
        User user = new User("ana", "ana@unillanos.edu.co", new Credential("hash", "sal"));
        usuarios.save(user);

        user.deactivate();
        usuarios.save(user);

        assertFalse(usuarios.findByUsernameOrEmail("ana").orElseThrow().isActive());
    }
}
