package com.tecno.Smartparking.websocket;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Si la ESP32 pierde energia de golpe no cierra el WebSocket y la sesion
 * queda huerfana. Esta vigilancia la cierra cuando dejan de llegar latidos.
 */
@Component
public class VigilanteLatidos {

    private static final Logger LOG = LoggerFactory.getLogger(VigilanteLatidos.class);

    private final SesionDispositivo sesion;
    private final long latidoTimeoutMs;

    public VigilanteLatidos(SesionDispositivo sesion,
                            @Value("${smartparking.dispositivo.latido-timeout-ms:5000}") long latidoTimeoutMs) {
        this.sesion = sesion;
        this.latidoTimeoutMs = latidoTimeoutMs;
    }

    @Scheduled(fixedDelayString = "${smartparking.dispositivo.latido-timeout-ms:5000}")
    public void revisarLatidos() {
        if (!sesion.estaAbierta()) {
            return;
        }
        Duration silencio = Duration.between(sesion.getUltimoLatido(), Instant.now());
        if (silencio.toMillis() > latidoTimeoutMs) {
            LOG.warn("Sin latidos del dispositivo por {} ms; se cierra la sesion", silencio.toMillis());
            sesion.cerrar();
        }
    }
}
