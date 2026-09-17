package com.tecno.Smartparking.config;

import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.service.barrera.Barrera;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Las dos barreras son estado compartido: una sola instancia de cada una. */
@Configuration
public class BarrerasConfig {

    @Bean
    public Barrera barreraEntrada() {
        return new Barrera(TipoPunto.ENTRADA);
    }

    @Bean
    public Barrera barreraSalida() {
        return new Barrera(TipoPunto.SALIDA);
    }
}
