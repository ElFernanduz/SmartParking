package com.tecno.Smartparking.security;

import com.tecno.Smartparking.model.Permission;
import com.tecno.Smartparking.model.User;
import com.tecno.Smartparking.service.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Cada ruta de escritura exige un permiso concreto. Las consultas quedan
 * abiertas en la red local; las acciones de operador piden sesion iniciada.
 */
@Component
public class InterceptorAutorizacion implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(InterceptorAutorizacion.class);

    private static final Map<String, String> PERMISO_POR_RUTA = Map.of(
            "/api/control/barrera", Permission.BARRERA_CONTROL,
            "/api/control/emergencia", Permission.EMERGENCIA_CONTROL,
            "/api/control/sincronizar-cupos", Permission.CUPOS_SYNC,
            "/api/configuracion", Permission.CONFIG_UPDATE);

    private final AuthorizationService autorizacion;

    public InterceptorAutorizacion(AuthorizationService autorizacion) {
        this.autorizacion = autorizacion;
    }

    @Override
    public boolean preHandle(HttpServletRequest peticion, HttpServletResponse respuesta,
                             Object manejador) throws Exception {
        if (!"POST".equalsIgnoreCase(peticion.getMethod())) {
            return true;
        }

        String permisoRequerido = PERMISO_POR_RUTA.get(peticion.getRequestURI());
        if (permisoRequerido == null) {
            return true;
        }

        User usuario = SesionOperador.usuarioDe(peticion);
        if (usuario == null) {
            return rechazar(respuesta, HttpStatus.UNAUTHORIZED,
                    "Inicie sesion para ejecutar esta accion");
        }

        if (!autorizacion.hasPermission(usuario, permisoRequerido)) {
            LOG.warn("{} no tiene el permiso {} para {}", usuario.getUsername(),
                    permisoRequerido, peticion.getRequestURI());
            return rechazar(respuesta, HttpStatus.FORBIDDEN,
                    "Su rol no tiene el permiso " + permisoRequerido);
        }

        return true;
    }

    private boolean rechazar(HttpServletResponse respuesta, HttpStatus estado, String mensaje)
            throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write(
                "{\"estado\":" + estado.value() + ",\"mensaje\":\"" + mensaje + "\"}");
        return false;
    }
}
