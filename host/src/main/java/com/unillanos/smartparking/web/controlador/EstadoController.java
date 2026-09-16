package com.unillanos.smartparking.web.controlador;

import com.unillanos.smartparking.dominio.puerto.entrada.ConsultarEstadoCasoUso;
import com.unillanos.smartparking.web.dto.EstadoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estado")
public class EstadoController {

    private final ConsultarEstadoCasoUso consultarEstado;

    public EstadoController(ConsultarEstadoCasoUso consultarEstado) {
        this.consultarEstado = consultarEstado;
    }

    @GetMapping
    public EstadoResponse obtenerEstado() {
        return EstadoResponse.desde(consultarEstado.obtenerEstado());
    }
}
