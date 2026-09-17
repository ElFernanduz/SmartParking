package com.tecno.Smartparking.controller;

import com.tecno.Smartparking.dto.EventoSeguridadResponse;
import com.tecno.Smartparking.dto.RegistroResponse;
import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.service.HistorialService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistorialController {

    private static final int LIMITE_MAXIMO = 500;

    private final HistorialService historial;

    public HistorialController(HistorialService historial) {
        this.historial = historial;
    }

    @GetMapping("/api/registros")
    public List<RegistroResponse> obtenerRegistros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) EstadoVisita estadoVisita,
            @RequestParam(required = false, defaultValue = "100") int limite) {
        return historial.buscarRegistros(desde, hasta, estadoVisita, acotar(limite)).stream()
                .map(RegistroResponse::desde)
                .toList();
    }

    @GetMapping("/api/eventos-seguridad")
    public List<EventoSeguridadResponse> obtenerEventosSeguridad(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false, defaultValue = "100") int limite) {
        return historial.buscarEventosSeguridad(desde, hasta, acotar(limite)).stream()
                .map(EventoSeguridadResponse::desde)
                .toList();
    }

    private int acotar(int limite) {
        return Math.min(Math.max(limite, 1), LIMITE_MAXIMO);
    }
}
