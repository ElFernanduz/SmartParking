#ifndef SENSOR_HUMO_H
#define SENSOR_HUMO_H

#include <Arduino.h>

/**
 * MQ-2 leido por una entrada del ADC1. El calentador del sensor necesita
 * estabilizarse: durante la ventana de calentamiento las lecturas son altas
 * y falsas, asi que se ignoran.
 */
class SensorHumo {
public:
    SensorHumo(uint8_t pinAdc, unsigned long msCalentamiento);

    void iniciar();

    /** Nivel suavizado de 0 a 4095. */
    int leerNivel();

    bool estaCalentando() const;

private:
    uint8_t pinAdc;
    unsigned long msCalentamiento;
    unsigned long encendidoEn;
    int nivelSuavizado;
};

#endif
