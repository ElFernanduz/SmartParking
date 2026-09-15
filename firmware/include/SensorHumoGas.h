#ifndef SENSOR_HUMO_GAS_H
#define SENSOR_HUMO_GAS_H

#include "Sensor.h"

// Sensor MQ-2: detecta humo y gas.
class SensorHumoGas : public Sensor {
private:
    float nivelConcentracion;
    int pin;
    float umbralAnomalia;

public:
    SensorHumoGas(const String &id, int pin, float umbralAnomalia = 400.0f)
        : Sensor(id), nivelConcentracion(0.0f), pin(pin), umbralAnomalia(umbralAnomalia) {}

    float leerDatos() override {
        nivelConcentracion = analogRead(pin);
        return nivelConcentracion;
    }

    bool detectarAnomalia() {
        return nivelConcentracion >= umbralAnomalia;
    }

    float getNivelConcentracion() const { return nivelConcentracion; }
};

#endif
