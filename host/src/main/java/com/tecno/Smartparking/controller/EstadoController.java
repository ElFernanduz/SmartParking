package com.tecno.Smartparking.controller;

import com.tecno.Smartparking.dto.EstadoResponse;
import com.tecno.Smartparking.service.AlarmaService;
import com.tecno.Smartparking.service.DispositivoService;
import com.tecno.Smartparking.service.EstadoParqueaderoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estado")
public class EstadoController {

    private final EstadoParqueaderoService estado;
    private final AlarmaService alarma;
    private final DispositivoService dispositivo;

    public EstadoController(EstadoParqueaderoService estado, AlarmaService alarma,
                            DispositivoService dispositivo) {
        this.estado = estado;
        this.alarma = alarma;
        this.dispositivo = dispositivo;
    }

    @GetMapping
    public EstadoResponse obtenerEstado() {
        return new EstadoResponse(estado.getCuposDisponibles(), estado.getCapacidadTotal(),
                estado.getCuposOcupados(), estado.getEstado().name(),
                alarma.estaActiva(), dispositivo.estaConectado());
    }
}
