package com.unillanos.smartparking.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import com.unillanos.smartparking.dominio.modelo.EstadoVisita;
import com.unillanos.smartparking.dominio.modelo.EventoSeguridad;
import com.unillanos.smartparking.dominio.modelo.EventoSistema;
import com.unillanos.smartparking.dominio.modelo.FiltroEventos;
import com.unillanos.smartparking.dominio.modelo.FiltroRegistros;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.modelo.TipoVehiculo;
import com.unillanos.smartparking.dominio.modelo.Vehiculo;
import com.unillanos.smartparking.infraestructura.persistencia.adaptador.RepositorioConfiguracionAdapter;
import com.unillanos.smartparking.infraestructura.persistencia.adaptador.RepositorioEventoSeguridadAdapter;
import com.unillanos.smartparking.infraestructura.persistencia.adaptador.RepositorioEventoSistemaAdapter;
import com.unillanos.smartparking.infraestructura.persistencia.adaptador.RepositorioRegistroAccesoAdapter;
import com.unillanos.smartparking.infraestructura.persistencia.adaptador.RepositorioVehiculoAdapter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RepositorioRegistroAccesoAdapter.class, RepositorioEventoSeguridadAdapter.class,
        RepositorioEventoSistemaAdapter.class, RepositorioConfiguracionAdapter.class,
        RepositorioVehiculoAdapter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/smartparking-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:db/schema.sql"
})
class PersistenciaAdaptadoresTest {

    @Autowired
    private RepositorioRegistroAccesoAdapter registros;
    @Autowired
    private RepositorioEventoSeguridadAdapter eventosSeguridad;
    @Autowired
    private RepositorioEventoSistemaAdapter eventosSistema;
    @Autowired
    private RepositorioConfiguracionAdapter configuracion;
    @Autowired
    private RepositorioVehiculoAdapter vehiculos;

    @Test
    @DisplayName("Guardar una visita le asigna identificador y la deja contada como activa")
    void guardarVisitaActiva() {
        RegistroAcceso registro = registros.guardar(RegistroAcceso.nuevaVisita(LocalDateTime.now()));

        assertNotNull(registro.getId());
        assertEquals(1, registros.contarActivos());
    }

    @Test
    @DisplayName("La salida se empareja con el ingreso activo mas antiguo")
    void elActivoMasAntiguoEsElPrimero() {
        LocalDateTime base = LocalDateTime.of(2026, 3, 1, 7, 0);
        registros.guardar(RegistroAcceso.nuevaVisita(base.plusHours(2)));
        registros.guardar(RegistroAcceso.nuevaVisita(base));

        Optional<RegistroAcceso> masAntiguo = registros.buscarActivoMasAntiguo();

        assertTrue(masAntiguo.isPresent());
        assertEquals(base, masAntiguo.get().getHoraEntrada());
    }

    @Test
    @DisplayName("Finalizar una visita la saca del conteo de activos")
    void finalizarVisita() {
        LocalDateTime entrada = LocalDateTime.of(2026, 3, 1, 9, 0);
        RegistroAcceso registro = registros.guardar(RegistroAcceso.nuevaVisita(entrada));

        registro.finalizar(entrada.plusMinutes(45));
        registros.guardar(registro);

        assertEquals(0, registros.contarActivos());
        List<RegistroAcceso> finalizados = registros.buscarTodos(
                new FiltroRegistros(null, null, EstadoVisita.FINALIZADO, 10));
        assertEquals(1, finalizados.size());
        assertEquals(45, finalizados.get(0).duracion().toMinutes());
    }

    @Test
    @DisplayName("Eliminar descarta la visita reservada que nunca ocurrio")
    void eliminarVisita() {
        RegistroAcceso registro = registros.guardar(RegistroAcceso.nuevaVisita(LocalDateTime.now()));

        registros.eliminar(registro);

        assertEquals(0, registros.contarActivos());
    }

    @Test
    @DisplayName("El filtro por rango de fechas acota el historial")
    void filtroPorFechas() {
        LocalDateTime base = LocalDateTime.of(2026, 3, 1, 10, 0);
        registros.guardar(RegistroAcceso.nuevaVisita(base.minusDays(5)));
        registros.guardar(RegistroAcceso.nuevaVisita(base));

        List<RegistroAcceso> recientes = registros.buscarTodos(
                new FiltroRegistros(base.minusDays(1), base.plusDays(1), null, 10));

        assertEquals(1, recientes.size());
        assertEquals(base, recientes.get(0).getHoraEntrada());
    }

    @Test
    @DisplayName("Un evento de seguridad conserva nivel, umbral y bandera de evacuacion")
    void eventoDeSeguridad() {
        LocalDateTime cuando = LocalDateTime.of(2026, 3, 1, 11, 30);
        eventosSeguridad.guardar(new EventoSeguridad(null, 850, 400, cuando, true));

        List<EventoSeguridad> encontrados = eventosSeguridad.buscarTodos(FiltroEventos.sinFiltro());

        assertEquals(1, encontrados.size());
        assertEquals(850, encontrados.get(0).getNivelGasRegistrado());
        assertTrue(encontrados.get(0).requiereEvacuacion());
        assertEquals(cuando, encontrados.get(0).getTimestamp());
    }

    @Test
    @DisplayName("La bitacora del sistema recibe identificador al guardarse")
    void bitacoraDelSistema() {
        EventoSistema evento = eventosSistema.guardar(EventoSistema.de("PRUEBA", "detalle"));

        assertNotNull(evento.getId());
    }

    @Test
    @DisplayName("La configuracion arranca con los valores del esquema y se puede actualizar")
    void configuracionPorDefectoYActualizacion() {
        assertEquals(6, configuracion.obtenerCapacidad());
        assertEquals(400, configuracion.obtenerUmbral());
        assertEquals(EstadoOperativo.OPERATIVO, configuracion.obtenerEstadoSistema());

        configuracion.guardarCapacidad(12);
        configuracion.guardarUmbral(750);
        configuracion.guardarEstadoSistema(EstadoOperativo.EMERGENCIA);

        assertEquals(12, configuracion.obtenerCapacidad());
        assertEquals(750, configuracion.obtenerUmbral());
        assertEquals(EstadoOperativo.EMERGENCIA, configuracion.obtenerEstadoSistema());
    }

    @Test
    @DisplayName("Un vehiculo se guarda y se recupera por placa")
    void vehiculoPorPlaca() {
        LocalDateTime registro = LocalDateTime.of(2026, 3, 1, 8, 15);
        vehiculos.guardar(new Vehiculo("XYZ987", TipoVehiculo.TURBO, registro));

        Optional<Vehiculo> encontrado = vehiculos.buscarPorPlaca("XYZ987");

        assertTrue(encontrado.isPresent());
        assertEquals(TipoVehiculo.TURBO, encontrado.get().getTipoVehiculo());
        assertEquals(registro, encontrado.get().getFechaRegistro());
        assertFalse(vehiculos.buscarPorPlaca("NOEXISTE").isPresent());
    }
}
