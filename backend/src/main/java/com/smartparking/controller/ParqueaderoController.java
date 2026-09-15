package com.smartparking.controller;

import com.smartparking.dto.ActualizarEspaciosRequest;
import com.smartparking.model.Parqueadero;
import com.smartparking.service.ParqueaderoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parqueadero")
public class ParqueaderoController {

    private final ParqueaderoService parqueaderoService;

    public ParqueaderoController(ParqueaderoService parqueaderoService) {
        this.parqueaderoService = parqueaderoService;
    }

    // Consultado por el ESP32 (DisplayContador) y por cualquier dashboard web.
    @GetMapping
    public Parqueadero obtenerEstado() {
        return parqueaderoService.obtenerParqueadero();
    }

    // Invocado por el ESP32 (ApiCliente) cuando ControlAcceso detecta un ingreso o salida.
    @PostMapping("/actualizar")
    public Parqueadero actualizar(@RequestBody ActualizarEspaciosRequest request) {
        return parqueaderoService.actualizarEspacios(request.getCantidad());
    }
}
