#include "ControladorAccesoFisico.h"

ControladorAccesoFisico::ControladorAccesoFisico(const char* punto, uint8_t pinSensor,
                                                 uint8_t pinServo, unsigned long msAntirrebote,
                                                 int anguloCerrado, int anguloAbierto)
    : punto(punto), sensor(pinSensor, msAntirrebote),
      motor(pinServo, anguloCerrado, anguloAbierto), ocupadoAntes(false),
      presenciaReportada(false), cierrePendiente(false), bloqueoReportado(false) {
}

void ControladorAccesoFisico::iniciar() {
    sensor.iniciar();
    motor.iniciar();
}

EventoAcceso ControladorAccesoFisico::actualizar() {
    motor.actualizar();
    bool ocupado = sensor.leerEstado();
    EventoAcceso evento = EventoAcceso::NINGUNO;

    if (ocupado && !ocupadoAntes && !presenciaReportada) {
        presenciaReportada = true;
        evento = EventoAcceso::PRESENCIA_DETECTADA;
    }

    // El paso se confirma solo cuando el sensor queda libre de forma estable.
    if (!ocupado && ocupadoAntes && motor.estaAbierta()) {
        presenciaReportada = false;
        evento = EventoAcceso::PASO_COMPLETADO;
    }

    if (!ocupado && !ocupadoAntes) {
        presenciaReportada = false;
    }

    // Condicion segura: la barrera baja apenas el punto queda libre, nunca antes.
    if (cierrePendiente) {
        if (!ocupado) {
            motor.cerrar();
            cierrePendiente = false;
            bloqueoReportado = false;
        } else if (!bloqueoReportado) {
            bloqueoReportado = true;
            evento = EventoAcceso::CIERRE_BLOQUEADO;
        }
    }

    ocupadoAntes = ocupado;
    return evento;
}

void ControladorAccesoFisico::ejecutarApertura() {
    cierrePendiente = false;
    bloqueoReportado = false;
    motor.abrir();
}

void ControladorAccesoFisico::ejecutarCierre() {
    cierrePendiente = true;
    bloqueoReportado = false;
}

const char* ControladorAccesoFisico::nombrePunto() const {
    return punto;
}

bool ControladorAccesoFisico::estaAbierta() const {
    return motor.estaAbierta();
}
