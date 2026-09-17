package com.tecno.Smartparking.controller;

import com.tecno.Smartparking.dto.BarreraRequest;
import com.tecno.Smartparking.dto.EmergenciaRequest;
import com.tecno.Smartparking.dto.SincronizarCuposRequest;
import com.tecno.Smartparking.service.ControlManualService;
import com.tecno.Smartparking.service.EmergenciaService;
import com.tecno.Smartparking.service.SincronizacionCuposService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control")
public class ControlController {

    private final ControlManualService controlManual;
    private final EmergenciaService emergencia;
    private final SincronizacionCuposService sincronizacion;

    public ControlController(ControlManualService controlManual,
                             EmergenciaService emergencia,
                             SincronizacionCuposService sincronizacion) {
        this.controlManual = controlManual;
        this.emergencia = emergencia;
        this.sincronizacion = sincronizacion;
    }

    @PostMapping("/barrera")
    public ResponseEntity<Void> controlarBarrera(@Valid @RequestBody BarreraRequest peticion) {
        if ("ABRIR".equals(peticion.accion())) {
            controlManual.abrirBarrera(peticion.punto());
        } else {
            controlManual.cerrarBarrera(peticion.punto());
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/emergencia")
    public ResponseEntity<Void> controlarEmergencia(@Valid @RequestBody EmergenciaRequest peticion) {
        if ("ACTIVAR".equals(peticion.accion())) {
            emergencia.activar();
        } else {
            emergencia.limpiar();
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/sincronizar-cupos")
    public ResponseEntity<Void> sincronizarCupos(@Valid @RequestBody SincronizarCuposRequest peticion) {
        sincronizacion.fijarCuposOcupados(peticion.cuposOcupados());
        return ResponseEntity.accepted().build();
    }
}
