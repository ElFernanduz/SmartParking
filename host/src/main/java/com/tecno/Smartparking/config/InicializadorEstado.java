package com.tecno.Smartparking.config;

import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.repository.RegistroAccesoRepository;
import com.tecno.Smartparking.service.ConfiguracionService;
import com.tecno.Smartparking.service.DispositivoService;
import com.tecno.Smartparking.service.EstadoParqueaderoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Recuperacion tras reinicio: el conteo y el estado se reconstruyen desde la
 * base, contando las visitas que quedaron activas.
 */
@Component
@Order(2)
public class InicializadorEstado implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorEstado.class);

    private final EstadoParqueaderoService estado;
    private final ConfiguracionService configuracion;
    private final RegistroAccesoRepository registros;
    private final DispositivoService dispositivo;

    public InicializadorEstado(EstadoParqueaderoService estado,
                               ConfiguracionService configuracion,
                               RegistroAccesoRepository registros,
                               DispositivoService dispositivo) {
        this.estado = estado;
        this.configuracion = configuracion;
        this.registros = registros;
        this.dispositivo = dispositivo;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        int capacidad = configuracion.obtenerCapacidad();
        int umbral = configuracion.obtenerUmbral();
        int ocupados = registros.countByEstadoVisita(EstadoVisita.ACTIVO);

        estado.inicializar(capacidad, ocupados, configuracion.obtenerEstadoSistema());

        // Si la ESP32 ya esta conectada se le propaga el umbral; si no, se hace al llegar su LISTO.
        if (dispositivo.estaConectado()) {
            dispositivo.configurarUmbral(umbral);
            dispositivo.actualizarPantalla(estado.getCuposDisponibles());
        }

        LOG.info("Estado restablecido: {} de {} cupos libres, umbral {}, sistema {}",
                estado.getCuposDisponibles(), capacidad, umbral, estado.getEstado());
    }
}
