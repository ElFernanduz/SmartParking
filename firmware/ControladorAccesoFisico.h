#ifndef CONTROLADOR_ACCESO_FISICO_H
#define CONTROLADOR_ACCESO_FISICO_H

#include <Arduino.h>
#include "BarreraMotor.h"
#include "SensorInfrarrojo.h"

/** Lo que el punto de acceso tiene para reportar al backend en este ciclo. */
enum class EventoAcceso {
    NINGUNO,
    PRESENCIA_DETECTADA,
    PASO_COMPLETADO,
    CIERRE_BLOQUEADO
};

/**
 * Coordina el sensor y el servo de un punto. El firmware solo detecta y
 * ejecuta: la decision de abrir es del backend. La unica regla local es la
 * condicion segura, que impide bajar la barrera sobre un vehiculo.
 */
class ControladorAccesoFisico {
public:
    ControladorAccesoFisico(const char* punto, uint8_t pinSensor, uint8_t pinServo,
                            unsigned long msAntirrebote, int anguloCerrado, int anguloAbierto);

    void iniciar();
    EventoAcceso actualizar();

    void ejecutarApertura();
    void ejecutarCierre();

    const char* nombrePunto() const;
    bool estaAbierta() const;

private:
    const char* punto;
    SensorInfrarrojo sensor;
    BarreraMotor motor;
    bool ocupadoAntes;
    bool presenciaReportada;
    bool cierrePendiente;
    bool bloqueoReportado;
};

#endif
