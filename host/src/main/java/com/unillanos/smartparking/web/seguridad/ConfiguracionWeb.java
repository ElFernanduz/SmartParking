package com.unillanos.smartparking.web.seguridad;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final InterceptorAutorizacion interceptor;

    public ConfiguracionWeb(InterceptorAutorizacion interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(interceptor)
                .addPathPatterns("/api/configuracion", "/api/control/**");
    }
}
