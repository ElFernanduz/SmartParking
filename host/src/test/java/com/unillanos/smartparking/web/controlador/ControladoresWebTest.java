package com.unillanos.smartparking.web.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.unillanos.smartparking.web.seguridad.InterceptorTokenOperador;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {EstadoController.class, HistorialController.class,
        ConfiguracionController.class, ControlController.class})
@Import({ConfiguracionWeb.class, InterceptorTokenOperador.class,
        ManejadorGlobalErrores.class})
@TestPropertySource(properties = "smartparking.seguridad.token-operador=token-de-prueba")
class ControladoresWebTest {

    private static final String TOKEN = "token-de-prueba";

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

    @Test
    @DisplayName("GET /api/estado devuelve cupos, capacidad, estado y alarma")
    void consultaDeEstado() throws Exception {
        when(consultarEstado.obtenerEstado())
                .thenReturn(new EstadoActual(4, 6, EstadoOperativo.OPERATIVO, false, true));

        mockMvc.perform(get("/api/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuposDisponibles").value(4))
                .andExpect(jsonPath("$.capacidadTotal").value(6))
                .andExpect(jsonPath("$.cuposOcupados").value(2))
                .andExpect(jsonPath("$.estado").value("OPERATIVO"))
                .andExpect(jsonPath("$.alarmaActiva").value(false))
                .andExpect(jsonPath("$.dispositivoConectado").value(true));
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
                .andExpect(jsonPath("$[0].estadoVisita").value("FINALIZADO"))
                .andExpect(jsonPath("$[0].duracionMinutos").value(30));
    }

    @Test
    @DisplayName("GET /api/configuracion queda abierto a lectura")
    void consultaDeConfiguracionSinToken() throws Exception {
        when(gestionarConfiguracion.obtenerConfiguracion())
                .thenReturn(new Configuracion(6, 400, EstadoOperativo.OPERATIVO));

        mockMvc.perform(get("/api/configuracion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidadTotal").value(6))
                .andExpect(jsonPath("$.umbralHumo").value(400));
    }

    @Test
    @DisplayName("Sin token de operador las escrituras se rechazan")
    void escrituraSinTokenSeRechaza() throws Exception {
        mockMvc.perform(post("/api/control/barrera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"punto\":\"ENTRADA\",\"accion\":\"ABRIR\"}"))
                .andExpect(status().isUnauthorized());

        verify(controlManual, never()).abrirBarrera(any());
    }

    @Test
    @DisplayName("Con token de operador se abre la barrera pedida")
    void aperturaManualDeBarrera() throws Exception {
        mockMvc.perform(post("/api/control/barrera")
                        .header(InterceptorTokenOperador.CABECERA, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"punto\":\"SALIDA\",\"accion\":\"ABRIR\"}"))
                .andExpect(status().isAccepted());

        verify(controlManual).abrirBarrera(TipoPunto.SALIDA);
    }

    @Test
    @DisplayName("La emergencia se puede forzar y limpiar desde el tablero")
    void controlDeEmergencia() throws Exception {
        mockMvc.perform(post("/api/control/emergencia")
                        .header(InterceptorTokenOperador.CABECERA, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"ACTIVAR\"}"))
                .andExpect(status().isAccepted());
        mockMvc.perform(post("/api/control/emergencia")
                        .header(InterceptorTokenOperador.CABECERA, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"LIMPIAR\"}"))
                .andExpect(status().isAccepted());

        verify(emergencia).activarEmergencia();
        verify(emergencia).limpiarEmergencia();
    }

    @Test
    @DisplayName("Una accion no valida se rechaza con 400")
    void accionInvalida() throws Exception {
        mockMvc.perform(post("/api/control/emergencia")
                        .header(InterceptorTokenOperador.CABECERA, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accion\":\"EXPLOTAR\"}"))
                .andExpect(status().isBadRequest());

        verify(emergencia, never()).activarEmergencia();
    }

    @Test
    @DisplayName("Actualizar la configuracion aplica capacidad y umbral")
    void actualizacionDeConfiguracion() throws Exception {
        when(gestionarConfiguracion.obtenerConfiguracion())
                .thenReturn(new Configuracion(10, 500, EstadoOperativo.OPERATIVO));

        mockMvc.perform(post("/api/configuracion")
                        .header(InterceptorTokenOperador.CABECERA, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capacidadTotal\":10,\"umbralHumo\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidadTotal").value(10));

        verify(gestionarConfiguracion).actualizarCapacidad(10);
        verify(gestionarConfiguracion).actualizarUmbral(500);
    }

    @Test
    @DisplayName("Una excepcion de dominio se traduce a 400 con cuerpo de error")
    void excepcionDeDominioSeTraduce() throws Exception {
        org.mockito.Mockito.doThrow(new ValorInvalidoExcepcion("Los cupos ocupados no son validos"))
                .when(sincronizarCupos).fijarCuposOcupados(99);

        mockMvc.perform(post("/api/control/sincronizar-cupos")
                        .header(InterceptorTokenOperador.CABECERA, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cuposOcupados\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("Los cupos ocupados no son validos"));
    }
}
