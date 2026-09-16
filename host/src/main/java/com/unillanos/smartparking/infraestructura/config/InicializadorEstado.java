package com.unillanos.smartparking.infraestructura.config;

import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioConfiguracion;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Recuperacion tras reinicio: el conteo y el estado se reconstruyen desde la
 * base, contando las visitas que quedaron activas.
 */
@Component
public class InicializadorEstado implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorEstado.class);

    private final EstadoParqueadero estadoParqueadero;
    private final RepositorioConfiguracion repositorioConfiguracion;
    private final RepositorioRegistroAcceso repositorioRegistros;
    private final PuertoPasarelaDispositivo pasarela;

    public InicializadorEstado(EstadoParqueadero estadoParqueadero,
                               RepositorioConfiguracion repositorioConfiguracion,
                               RepositorioRegistroAcceso repositorioRegistros,
                               PuertoPasarelaDispositivo pasarela) {
        this.estadoParqueadero = estadoParqueadero;
        this.repositorioConfiguracion = repositorioConfiguracion;
        this.repositorioRegistros = repositorioRegistros;
        this.pasarela = pasarela;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        int capacidad = repositorioConfiguracion.obtenerCapacidad();
        int umbral = repositorioConfiguracion.obtenerUmbral();
        EstadoOperativo estadoGuardado = repositorioConfiguracion.obtenerEstadoSistema();

        estadoParqueadero.actualizarCapacidad(capacidad);
        int ocupados = Math.min(repositorioRegistros.contarActivos(), capacidad);
        estadoParqueadero.fijarCuposDisponibles(capacidad - ocupados);
        estadoParqueadero.cambiarEstado(estadoGuardado);

        // Si la ESP32 ya esta conectada se le propaga el umbral; si no, se hace al llegar su LISTO.
        if (pasarela.estaConectado()) {
            pasarela.configurarUmbral(umbral);
            pasarela.actualizarPantalla(estadoParqueadero.getCuposDisponibles());
        }

        LOG.info("Estado restablecido: {} de {} cupos libres, umbral {}, sistema {}",
                estadoParqueadero.getCuposDisponibles(), capacidad, umbral,
                estadoParqueadero.getEstado());
    }
}
