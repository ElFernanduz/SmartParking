package com.tecno.Smartparking.controller;

import com.tecno.Smartparking.dto.ConfiguracionRequest;
import com.tecno.Smartparking.dto.ConfiguracionResponse;
import com.tecno.Smartparking.service.ConfiguracionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService configuracion;

    public ConfiguracionController(ConfiguracionService configuracion) {
        this.configuracion = configuracion;
    }

    @GetMapping
    public ConfiguracionResponse obtener() {
        return actual();
    }

    @PostMapping
    public ConfiguracionResponse actualizar(@Valid @RequestBody ConfiguracionRequest peticion) {
        if (peticion.capacidadTotal() != null) {
            configuracion.actualizarCapacidad(peticion.capacidadTotal());
        }
        if (peticion.umbralHumo() != null) {
            configuracion.actualizarUmbral(peticion.umbralHumo());
        }
        return actual();
    }

    private ConfiguracionResponse actual() {
        return new ConfiguracionResponse(configuracion.obtenerCapacidad(),
                configuracion.obtenerUmbral(), configuracion.obtenerEstadoSistema().name());
    }
}
