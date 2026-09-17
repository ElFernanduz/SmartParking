package com.tecno.Smartparking.service;

import com.tecno.Smartparking.model.EventoSeguridad;
import com.tecno.Smartparking.repository.EventoSeguridadRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Compara la telemetria de humo contra el umbral configurado. */
@Service
public class HumoService {

    private static final Logger LOG = LoggerFactory.getLogger(HumoService.class);

    private final ConfiguracionService configuracion;
    private final EventoSeguridadRepository eventos;
    private final EmergenciaService emergencia;
    private final AlarmaService alarma;
    private final NotificacionService notificaciones;

    public HumoService(ConfiguracionService configuracion,
                       EventoSeguridadRepository eventos,
                       EmergenciaService emergencia,
                       AlarmaService alarma,
                       NotificacionService notificaciones) {
        this.configuracion = configuracion;
        this.eventos = eventos;
        this.emergencia = emergencia;
        this.alarma = alarma;
        this.notificaciones = notificaciones;
    }

    @Transactional
    public void procesarLectura(int nivel) {
        int umbral = configuracion.obtenerUmbral();
        if (nivel <= umbral) {
            return;
        }
        // Mientras la emergencia siga activa no se repite el registro por cada lectura.
        if (alarma.estaActiva()) {
            return;
        }
        LOG.warn("Umbral de humo superado: nivel {} sobre umbral {}", nivel, umbral);

        alarma.activar();
        eventos.save(new EventoSeguridad(nivel, umbral, LocalDateTime.now(), true));
        notificaciones.umbralHumoSuperado(nivel, umbral);

        emergencia.activar();
    }
}
