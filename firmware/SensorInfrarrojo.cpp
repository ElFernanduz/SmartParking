#include "SensorInfrarrojo.h"

// Los modulos IR de obstaculo ponen la salida en LOW cuando detectan algo.
static const int NIVEL_CON_OBSTACULO = LOW;

SensorInfrarrojo::SensorInfrarrojo(uint8_t pin, unsigned long msAntirrebote)
    : pin(pin), msAntirrebote(msAntirrebote), estadoEstable(false),
      ultimaLecturaCruda(false), cambioEn(0) {
}

void SensorInfrarrojo::iniciar() {
    pinMode(pin, INPUT_PULLUP);
    bool inicial = digitalRead(pin) == NIVEL_CON_OBSTACULO;
    estadoEstable = inicial;
    ultimaLecturaCruda = inicial;
    cambioEn = millis();
}

bool SensorInfrarrojo::leerEstado() {
    bool cruda = digitalRead(pin) == NIVEL_CON_OBSTACULO;

    if (cruda != ultimaLecturaCruda) {
        ultimaLecturaCruda = cruda;
        cambioEn = millis();
        return estadoEstable;
    }

    // La lectura solo se acepta si se sostuvo el tiempo de antirrebote.
    if (cruda != estadoEstable && (millis() - cambioEn) >= msAntirrebote) {
        estadoEstable = cruda;
    }
    return estadoEstable;
}
