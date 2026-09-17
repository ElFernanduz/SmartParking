package com.tecno.Smartparking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tecno.Smartparking.config.WebConfig;
import com.tecno.Smartparking.exception.ManejadorGlobalErrores;
import com.tecno.Smartparking.exception.NegocioException;
import com.tecno.Smartparking.model.Credential;
import com.tecno.Smartparking.model.EstadoOperativo;
import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.model.Permission;
import com.tecno.Smartparking.model.RegistroAcceso;
import com.tecno.Smartparking.model.Role;
import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.model.User;
import com.tecno.Smartparking.security.InterceptorAutorizacion;
import com.tecno.Smartparking.security.SesionOperador;
import com.tecno.Smartparking.service.AlarmaService;
import com.tecno.Smartparking.service.AuthenticationService;
import com.tecno.Smartparking.service.AuthorizationService;
import com.tecno.Smartparking.service.ConfiguracionService;
import com.tecno.Smartparking.service.ControlManualService;
import com.tecno.Smartparking.service.DispositivoService;
import com.tecno.Smartparking.service.EmergenciaService;
import com.tecno.Smartparking.service.EstadoParqueaderoService;
import com.tecno.Smartparking.service.HistorialService;
import com.tecno.Smartparking.service.SincronizacionCuposService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {EstadoController.class, HistorialController.class,
        ConfiguracionController.class, ControlController.class, AuthController.class})
@Import({WebConfig.class, InterceptorAutorizacion.class, ManejadorGlobalErrores.class,
        AuthorizationService.class})
class ControladoresWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstadoParqueaderoService estado;
    @MockitoBean
    private AlarmaService alarma;
    @MockitoBean
    private DispositivoService dispositivo;
    @MockitoBean
    private HistorialService historial;
    @MockitoBean
    private ConfiguracionService configuracion;
    @MockitoBean
    private ControlManualService controlManual;
    @MockitoBean
    private EmergenciaService emergencia;
    @MockitoBean
    private SincronizacionCuposService sincronizacion;
    @MockitoBean
    private AuthenticationService autenticacion;

    /** Sesion de un usuario con los permisos indicados. */
    private static MockHttpSession sesionCon(String... codigosDePermiso) {
        Role rol = new Role("PRUEBA");
        for (String codigo : codigosDePermiso) {
            rol.addPermission(new Permission(codigo));
        }
        User user = new User("operador", "operador@unillanos.edu.co", new Credential("h", "s"));
        user.addRole(rol);

        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(SesionOperador.ATRIBUTO, user);
        return sesion;
    }

    @Test
    @DisplayName("GET /api/estado devuelve cupos, capacidad, estado y alarma")
    void consultaDeEstado() throws Exception {
        when(estado.getCuposDisponibles()).thenReturn(4);
        when(estado.getCapacidadTotal()).thenReturn(6);
        when(estado.getCuposOcupados()).thenReturn(2);
        when(estado.getEstado()).thenReturn(EstadoOperativo.OPERATIVO);
        when(alarma.estaActiva()).thenReturn(false);
        when(dispositivo.estaConectado()).thenReturn(true);

        mockMvc.perform(get("/api/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuposDisponibles").value(4))
                .andExpect(jsonPath("$.cuposOcupados").value(2))
                .andExpect(jsonPath("$.estado").value("OPERATIVO"))
                .andExpect(jsonPath("$.dispositivoConectado").value(true));
    }

    @Test
    @DisplayName("GET /api/registros devuelve el historial de accesos")
    void consultaDeHistorial() throws Exception {
        LocalDateTime entrada = LocalDateTime.of(2026, 3, 1, 8, 0);
        RegistroAcceso registro = RegistroAcceso.nuevaVisita(entrada);
        registro.finalizar(entrada.plusMinutes(30));
        when(historial.buscarRegistros(any(), any(), any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(List.of(registro));

        mockMvc.perform(get("/api/registros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoVisita").value(EstadoVisita.FINALIZADO.name()))
                .andExpect(jsonPath("$[0].duracionMinutos").value(30));
    }

    @Test
    @DisplayName("Las consultas siguen abiertas sin iniciar sesion")
    void lasConsultasQuedanAbiertas() throws Exception {
        when(configuracion.obtenerCapacidad()).thenReturn(6);
        when(configuracion.obtenerUmbral()).thenReturn(400);
        when(configuracion.obtenerEstadoSistema()).thenReturn(EstadoOperativo.OPERATIVO);

        mockMvc.perform(get("/api/configuracion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidadTotal").value(6));
    }

    @Test
    @DisplayName("Sin sesion las escrituras responden 401")
    void escrituraSinSesion() throws Exception {
        mockMvc.perform(post("/api/control/barrera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"punto\":\"ENTRADA\",\"accion\":\"ABRIR\"}"))
                .andExpect(status().isUnauthorized());

        verify(controlManual, never()).abrirBarrera(any());
    }

    @Test
    @DisplayName("Con sesion pero sin el permiso la accion responde 403")
    void escrituraSinPermiso() throws Exception {
        mockMvc.perform(post("/api/control/barrera")
                        .session(sesionCon(Permission.CONFIG_UPDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"punto\":\"ENTRADA\",\"accion\":\"ABRIR\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Su rol no tiene el permiso BARRERA_CONTROL"));

        verify(controlManual, never()).abrirBarrera(any());
    }

    @Test
    @DisplayName("Con el permiso BARRERA_CONTROL se abre la barrera")
    void aperturaConPermiso() throws Exception {
        mockMvc.perform(post("/api/control/barrera")
                        .session(sesionCon(Permission.BARRERA_CONTROL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"punto\":\"SALIDA\",\"accion\":\"ABRIR\"}"))
                .andExpect(status().isAccepted());

        verify(controlManual).abrirBarrera(TipoPunto.SALIDA);
    }

    @Test
    @DisplayName("Cada ruta exige su propio permiso")
    void cadaRutaExigeSuPermiso() throws Exception {
        mockMvc.perform(post("/api/control/emergencia")
                        .session(sesionCon(Permission.EMERGENCIA_CONTROL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"ACTIVAR\"}"))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/control/emergencia")
                        .session(sesionCon(Permission.BARRERA_CONTROL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"LIMPIAR\"}"))
                .andExpect(status().isForbidden());

        verify(emergencia).activar();
        verify(emergencia, never()).limpiar();
    }

    @Test
    @DisplayName("Actualizar la configuracion exige el permiso CONFIG_UPDATE")
    void actualizacionDeConfiguracion() throws Exception {
        when(configuracion.obtenerCapacidad()).thenReturn(10);
        when(configuracion.obtenerUmbral()).thenReturn(500);
        when(configuracion.obtenerEstadoSistema()).thenReturn(EstadoOperativo.OPERATIVO);

        mockMvc.perform(post("/api/configuracion")
                        .session(sesionCon(Permission.CONFIG_UPDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capacidadTotal\":10,\"umbralHumo\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidadTotal").value(10));

        verify(configuracion).actualizarCapacidad(10);
        verify(configuracion).actualizarUmbral(500);
    }

    @Test
    @DisplayName("Una accion no valida se rechaza con 400")
    void accionInvalida() throws Exception {
        mockMvc.perform(post("/api/control/emergencia")
                        .session(sesionCon(Permission.EMERGENCIA_CONTROL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"EXPLOTAR\"}"))
                .andExpect(status().isBadRequest());

        verify(emergencia, never()).activar();
    }

    @Test
    @DisplayName("Un error de negocio se traduce a 400 con cuerpo de error")
    void errorDeNegocioSeTraduce() throws Exception {
        org.mockito.Mockito.doThrow(new NegocioException("Los cupos ocupados no son validos"))
                .when(sincronizacion).fijarCuposOcupados(99);

        mockMvc.perform(post("/api/control/sincronizar-cupos")
                        .session(sesionCon(Permission.CUPOS_SYNC))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cuposOcupados\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Los cupos ocupados no son validos"));
    }

    @Test
    @DisplayName("Un login correcto abre sesion y devuelve roles y permisos")
    void loginCorrecto() throws Exception {
        Role rol = new Role("ADMIN");
        rol.addPermission(new Permission(Permission.CONFIG_UPDATE));
        User admin = new User("admin", "admin@unillanos.edu.co", new Credential("h", "s"));
        admin.addRole(rol);
        when(autenticacion.login("admin", "secreta")).thenReturn(Optional.of(admin));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"contrasena\":\"secreta\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.permisos[0]").value("CONFIG_UPDATE"));
    }

    @Test
    @DisplayName("Un login fallido responde 401 sin abrir sesion")
    void loginFallido() throws Exception {
        when(autenticacion.login("admin", "mala")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"contrasena\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.autenticado").value(false));
    }

    @Test
    @DisplayName("La consulta de sesion informa si hay una abierta")
    void consultaDeSesion() throws Exception {
        mockMvc.perform(get("/api/auth/sesion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(false));

        mockMvc.perform(get("/api/auth/sesion").session(sesionCon(Permission.CUPOS_SYNC)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.permisos[0]").value("CUPOS_SYNC"));
    }

    @Test
    @DisplayName("Cerrar sesion deja la sesion anonima")
    void cierreDeSesion() throws Exception {
        mockMvc.perform(post("/api/auth/logout").session(sesionCon(Permission.CUPOS_SYNC)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(false));
    }
}
