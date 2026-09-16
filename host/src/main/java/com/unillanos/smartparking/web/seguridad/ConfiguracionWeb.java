package com.unillanos.smartparking.web.seguridad;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(PropiedadesSeguridad.class)
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final InterceptorTokenOperador interceptor;

    public ConfiguracionWeb(InterceptorTokenOperador interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(interceptor)
                .addPathPatterns("/api/configuracion", "/api/control/**");
    }
}
