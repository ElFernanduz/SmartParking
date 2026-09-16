#ifndef SENSOR_INFRARROJO_H
#define SENSOR_INFRARROJO_H

#include <Arduino.h>

/**
 * Sensor IR de obstaculo con antirrebote por tiempo. No bloquea: mide con
 * millis() que la senal se mantenga estable antes de dar el cambio por bueno.
 */
class SensorInfrarrojo {
public:
    SensorInfrarrojo(uint8_t pin, unsigned long msAntirrebote);

    void iniciar();

    /** Estado estable del punto: true si hay un vehiculo delante del sensor. */
    bool leerEstado();

private:
    uint8_t pin;
    unsigned long msAntirrebote;
    bool estadoEstable;
    bool ultimaLecturaCruda;
    unsigned long cambioEn;
};

#endif
