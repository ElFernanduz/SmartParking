package com.unillanos.smartparking.web.controlador;

import com.unillanos.smartparking.dominio.modelo.EstadoVisita;
import com.unillanos.smartparking.dominio.modelo.FiltroEventos;
import com.unillanos.smartparking.dominio.modelo.FiltroRegistros;
import com.unillanos.smartparking.dominio.puerto.entrada.ConsultarHistorialCasoUso;
import com.unillanos.smartparking.web.dto.EventoSeguridadResponse;
import com.unillanos.smartparking.web.dto.RegistroResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistorialController {

    private static final int LIMITE_MAXIMO = 500;

    private final ConsultarHistorialCasoUso consultarHistorial;

    public HistorialController(ConsultarHistorialCasoUso consultarHistorial) {
        this.consultarHistorial = consultarHistorial;
    }

    @GetMapping("/api/registros")
    public List<RegistroResponse> obtenerRegistros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) EstadoVisita estadoVisita,
            @RequestParam(required = false, defaultValue = "100") int limite) {
        FiltroRegistros filtro = new FiltroRegistros(desde, hasta, estadoVisita, acotar(limite));
        return consultarHistorial.obtenerRegistros(filtro).stream()
                .map(RegistroResponse::desde)
                .toList();
    }

    @GetMapping("/api/eventos-seguridad")
    public List<EventoSeguridadResponse> obtenerEventosSeguridad(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false, defaultValue = "100") int limite) {
        FiltroEventos filtro = new FiltroEventos(desde, hasta, acotar(limite));
        return consultarHistorial.obtenerEventosSeguridad(filtro).stream()
                .map(EventoSeguridadResponse::desde)
                .toList();
    }

    private int acotar(int limite) {
        return Math.min(Math.max(limite, 1), LIMITE_MAXIMO);
    }
}
