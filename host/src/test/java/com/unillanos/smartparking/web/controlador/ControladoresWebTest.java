package com.unillanos.smartparking.web.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unillanos.smartparking.aplicacion.seguridad.AuthenticationService;
import com.unillanos.smartparking.aplicacion.seguridad.AuthorizationService;
import com.unillanos.smartparking.dominio.autenticacion.Credential;
import com.unillanos.smartparking.dominio.autenticacion.Permission;
import com.unillanos.smartparking.dominio.autenticacion.Permisos;
import com.unillanos.smartparking.dominio.autenticacion.Role;
import com.unillanos.smartparking.dominio.autenticacion.User;
import com.unillanos.smartparking.dominio.excepcion.ValorInvalidoExcepcion;
import com.unillanos.smartparking.dominio.modelo.Configuracion;
import com.unillanos.smartparking.dominio.modelo.EstadoActual;
import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import com.unillanos.smartparking.dominio.modelo.EstadoVisita;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.entrada.ConsultarEstadoCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.ConsultarHistorialCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.ControlManualCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.EjecutarEmergenciaCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.GestionarConfiguracionCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.SincronizarCuposCasoUso;
import com.unillanos.smartparking.web.error.ManejadorGlobalErrores;
import com.unillanos.smartparking.web.seguridad.ConfiguracionWeb;
import com.unillanos.smartparking.web.seguridad.InterceptorAutorizacion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {EstadoController.class, HistorialController.class,
        ConfiguracionController.class, ControlController.class, AuthController.class})
@Import({ConfiguracionWeb.class, InterceptorAutorizacion.class, ManejadorGlobalErrores.class,
        AuthorizationService.class})
class ControladoresWebTest {

    private static final String ATRIBUTO_SESION = "smartparking.usuario";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarEstadoCasoUso consultarEstado;
    @MockitoBean
    private ConsultarHistorialCasoUso consultarHistorial;
    @MockitoBean
    private GestionarConfiguracionCasoUso gestionarConfiguracion;
    @MockitoBean
    private ControlManualCasoUso controlManual;
    @MockitoBean
    private EjecutarEmergenciaCasoUso emergencia;
    @MockitoBean
    private SincronizarCuposCasoUso sincronizarCupos;
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
        sesion.setAttribute(ATRIBUTO_SESION, user);
        return sesion;
    }

    @Test
    @DisplayName("GET /api/estado devuelve cupos, capacidad, estado y alarma")
    void consultaDeEstado() throws Exception {
        when(consultarEstado.obtenerEstado())
                .thenReturn(new EstadoActual(4, 6, EstadoOperativo.OPERATIVO, false, true));

        mockMvc.perform(get("/api/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuposDisponibles").value(4))
                .andExpect(jsonPath("$.cuposOcupados").value(2))
                .andExpect(jsonPath("$.estado").value("OPERATIVO"));
    }

    @Test
    @DisplayName("GET /api/registros devuelve el historial de accesos")
    void consultaDeHistorial() throws Exception {
        LocalDateTime entrada = LocalDateTime.of(2026, 3, 1, 8, 0);
        RegistroAcceso registro = new RegistroAcceso(3L, null, entrada, entrada.plusMinutes(30),
                EstadoVisita.FINALIZADO);
        when(consultarHistorial.obtenerRegistros(any())).thenReturn(List.of(registro));

        mockMvc.perform(get("/api/registros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].duracionMinutos").value(30));
    }

    @Test
    @DisplayName("Las consultas siguen abiertas sin iniciar sesion")
    void lasConsultasQuedanAbiertas() throws Exception {
        when(gestionarConfiguracion.obtenerConfiguracion())
                .thenReturn(new Configuracion(6, 400, EstadoOperativo.OPERATIVO));

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
                        .session(sesionCon(Permisos.CONFIG_UPDATE))
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
                        .session(sesionCon(Permisos.BARRERA_CONTROL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"punto\":\"SALIDA\",\"accion\":\"ABRIR\"}"))
                .andExpect(status().isAccepted());

        verify(controlManual).abrirBarrera(TipoPunto.SALIDA);
    }

    @Test
    @DisplayName("Cada ruta exige su propio permiso")
    void cadaRutaExigeSuPermiso() throws Exception {
        mockMvc.perform(post("/api/control/emergencia")
                        .session(sesionCon(Permisos.EMERGENCIA_CONTROL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"ACTIVAR\"}"))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/control/emergencia")
                        .session(sesionCon(Permisos.BARRERA_CONTROL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"LIMPIAR\"}"))
                .andExpect(status().isForbidden());

        verify(emergencia).activarEmergencia();
        verify(emergencia, never()).limpiarEmergencia();
    }

    @Test
    @DisplayName("Actualizar la configuracion exige el permiso CONFIG_UPDATE")
    void actualizacionDeConfiguracion() throws Exception {
        when(gestionarConfiguracion.obtenerConfiguracion())
                .thenReturn(new Configuracion(10, 500, EstadoOperativo.OPERATIVO));

        mockMvc.perform(post("/api/configuracion")
                        .session(sesionCon(Permisos.CONFIG_UPDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capacidadTotal\":10,\"umbralHumo\":500}"))
                .andExpect(status().isOk());

        verify(gestionarConfiguracion).actualizarCapacidad(10);
        verify(gestionarConfiguracion).actualizarUmbral(500);
    }

    @Test
    @DisplayName("Una accion no valida se rechaza con 400")
    void accionInvalida() throws Exception {
        mockMvc.perform(post("/api/control/emergencia")
                        .session(sesionCon(Permisos.EMERGENCIA_CONTROL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"EXPLOTAR\"}"))
                .andExpect(status().isBadRequest());

        verify(emergencia, never()).activarEmergencia();
    }

    @Test
    @DisplayName("Una excepcion de dominio se traduce a 400 con cuerpo de error")
    void excepcionDeDominioSeTraduce() throws Exception {
        org.mockito.Mockito.doThrow(new ValorInvalidoExcepcion("Los cupos ocupados no son validos"))
                .when(sincronizarCupos).fijarCuposOcupados(99);

        mockMvc.perform(post("/api/control/sincronizar-cupos")
                        .session(sesionCon(Permisos.CUPOS_SYNC))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cuposOcupados\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Los cupos ocupados no son validos"));
    }

    @Test
    @DisplayName("Un login correcto abre sesion y devuelve roles y permisos")
    void loginCorrecto() throws Exception {
        Role rol = new Role("ADMIN");
        rol.addPermission(new Permission(Permisos.CONFIG_UPDATE));
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

        mockMvc.perform(get("/api/auth/sesion").session(sesionCon(Permisos.CUPOS_SYNC)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.permisos[0]").value("CUPOS_SYNC"));
    }

    @Test
    @DisplayName("Cerrar sesion deja la sesion anonima")
    void cierreDeSesion() throws Exception {
        mockMvc.perform(post("/api/auth/logout").session(sesionCon(Permisos.CUPOS_SYNC)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(false));
    }
}
