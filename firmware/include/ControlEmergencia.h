#ifndef CONTROL_EMERGENCIA_H
#define CONTROL_EMERGENCIA_H

#include "SensorHumoGas.h"
#include "AlarmaBuzzer.h"

// Lee datos del sensor de humo/gas y controla el actuador AlarmaBuzzer.
class ControlEmergencia {
private:
    SensorHumoGas &sensorHumoGas;
    AlarmaBuzzer &alarmaBuzzer;

public:
    ControlEmergencia(SensorHumoGas &sensorHumoGas, AlarmaBuzzer &alarmaBuzzer)
        : sensorHumoGas(sensorHumoGas), alarmaBuzzer(alarmaBuzzer) {}

    void monitorearNiveles() {
        sensorHumoGas.leerDatos();
        gestionarAlarma();
    }

    void gestionarAlarma() {
        if (sensorHumoGas.detectarAnomalia()) {
            alarmaBuzzer.activar();
        } else {
            alarmaBuzzer.desactivar();
        }
    }
};

#endif
