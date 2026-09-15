#ifndef SENSOR_PROXIMIDAD_H
#define SENSOR_PROXIMIDAD_H

#include "Sensor.h"

// Sensor IR / ultrasónico: detecta la presencia de un vehículo.
class SensorProximidad : public Sensor {
private:
    float distanciaActual;
    int pin;
    float distanciaUmbral;

public:
    SensorProximidad(const String &id, int pin, float distanciaUmbral = 10.0f)
        : Sensor(id), distanciaActual(0.0f), pin(pin), distanciaUmbral(distanciaUmbral) {}

    float leerDatos() override {
        distanciaActual = analogRead(pin); // sustituir por lógica real (IR / HC-SR04)
        return distanciaActual;
    }

    bool detectarVehiculo() {
        return distanciaActual <= distanciaUmbral;
    }

    float getDistanciaActual() const { return distanciaActual; }
};

#endif
