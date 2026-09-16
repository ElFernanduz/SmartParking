package com.unillanos.smartparking.web.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Proteccion minima para el alcance: las rutas de lectura quedan abiertas en
 * la red local y las de control exigen el token del operador.
 */
@Component
public class InterceptorTokenOperador implements HandlerInterceptor {

    public static final String CABECERA = "X-Token-Operador";

    private static final Logger LOG = LoggerFactory.getLogger(InterceptorTokenOperador.class);

    private final PropiedadesSeguridad propiedades;

    public InterceptorTokenOperador(PropiedadesSeguridad propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public boolean preHandle(HttpServletRequest peticion, HttpServletResponse respuesta, Object manejador)
            throws Exception {
        // Solo las escrituras exigen token; las consultas quedan abiertas en la red local.
        if (!"POST".equalsIgnoreCase(peticion.getMethod())) {
            return true;
        }
        String token = peticion.getHeader(CABECERA);
        if (propiedades.getTokenOperador().equals(token)) {
            return true;
        }
        LOG.warn("Peticion de operador rechazada: {} {}", peticion.getMethod(), peticion.getRequestURI());
        respuesta.setStatus(HttpStatus.UNAUTHORIZED.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write("{\"estado\":401,\"mensaje\":\"Token de operador invalido\"}");
        return false;
    }
}
