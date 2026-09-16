package com.unillanos.smartparking.web.controlador;

import com.unillanos.smartparking.dominio.puerto.entrada.GestionarConfiguracionCasoUso;
import com.unillanos.smartparking.web.dto.ConfiguracionRequest;
import com.unillanos.smartparking.web.dto.ConfiguracionResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final GestionarConfiguracionCasoUso gestionarConfiguracion;

    public ConfiguracionController(GestionarConfiguracionCasoUso gestionarConfiguracion) {
        this.gestionarConfiguracion = gestionarConfiguracion;
    }

    @GetMapping
    public ConfiguracionResponse obtener() {
        return ConfiguracionResponse.desde(gestionarConfiguracion.obtenerConfiguracion());
    }

    @PostMapping
    public ConfiguracionResponse actualizar(@Valid @RequestBody ConfiguracionRequest peticion) {
        if (peticion.capacidadTotal() != null) {
            gestionarConfiguracion.actualizarCapacidad(peticion.capacidadTotal());
        }
        if (peticion.umbralHumo() != null) {
            gestionarConfiguracion.actualizarUmbral(peticion.umbralHumo());
        }
        return ConfiguracionResponse.desde(gestionarConfiguracion.obtenerConfiguracion());
    }
}
