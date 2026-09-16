package com.unillanos.smartparking.infraestructura.dispositivo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartparking.dispositivo")
public class PropiedadesDispositivo {

    /** Token con el que la ESP32 se identifica al abrir la conexion. */
    private String token = "cambia-este-token";

    /** Tolerancia sin recibir latidos antes de dar la sesion por muerta. */
    private long latidoTimeoutMs = 5000;

    /** Espera maxima por el paso del vehiculo tras abrir una barrera. */
    private long esperaPasoMs = 10000;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getLatidoTimeoutMs() {
        return latidoTimeoutMs;
    }

    public void setLatidoTimeoutMs(long latidoTimeoutMs) {
        this.latidoTimeoutMs = latidoTimeoutMs;
    }

    public long getEsperaPasoMs() {
        return esperaPasoMs;
    }

    public void setEsperaPasoMs(long esperaPasoMs) {
        this.esperaPasoMs = esperaPasoMs;
    }
}
