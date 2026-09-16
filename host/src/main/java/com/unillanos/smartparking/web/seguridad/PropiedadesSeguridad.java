package com.unillanos.smartparking.web.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartparking.seguridad")
public class PropiedadesSeguridad {

    /** Token que deben traer las rutas de control y configuracion. */
    private String tokenOperador = "cambia-este-token";

    public String getTokenOperador() {
        return tokenOperador;
    }

    public void setTokenOperador(String tokenOperador) {
        this.tokenOperador = tokenOperador;
    }
}
