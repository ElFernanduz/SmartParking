package com.tecno.Smartparking.config;

import com.tecno.Smartparking.security.InterceptorAutorizacion;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final InterceptorAutorizacion interceptor;

    public WebConfig(InterceptorAutorizacion interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(interceptor)
                .addPathPatterns("/api/configuracion", "/api/control/**");
    }
}
