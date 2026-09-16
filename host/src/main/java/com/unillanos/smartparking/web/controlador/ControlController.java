package com.unillanos.smartparking.web.controlador;

import com.unillanos.smartparking.dominio.puerto.entrada.ControlManualCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.EjecutarEmergenciaCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.SincronizarCuposCasoUso;
import com.unillanos.smartparking.web.dto.BarreraCommandRequest;
import com.unillanos.smartparking.web.dto.EmergenciaRequest;
import com.unillanos.smartparking.web.dto.SincronizarCuposRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control")
public class ControlController {

    private final ControlManualCasoUso controlManual;
    private final EjecutarEmergenciaCasoUso emergencia;
    private final SincronizarCuposCasoUso sincronizarCupos;

    public ControlController(ControlManualCasoUso controlManual,
                             EjecutarEmergenciaCasoUso emergencia,
                             SincronizarCuposCasoUso sincronizarCupos) {
        this.controlManual = controlManual;
        this.emergencia = emergencia;
        this.sincronizarCupos = sincronizarCupos;
    }

    @PostMapping("/barrera")
    public ResponseEntity<Void> controlarBarrera(@Valid @RequestBody BarreraCommandRequest peticion) {
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
            emergencia.activarEmergencia();
        } else {
            emergencia.limpiarEmergencia();
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/sincronizar-cupos")
    public ResponseEntity<Void> sincronizarCupos(@Valid @RequestBody SincronizarCuposRequest peticion) {
        sincronizarCupos.fijarCuposOcupados(peticion.cuposOcupados());
        return ResponseEntity.accepted().build();
    }
}
