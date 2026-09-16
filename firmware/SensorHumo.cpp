#include "SensorHumo.h"

// Media movil exponencial simple: filtra el ruido sin guardar historial.
static const float PESO_NUEVA_LECTURA = 0.2f;

SensorHumo::SensorHumo(uint8_t pinAdc, unsigned long msCalentamiento)
    : pinAdc(pinAdc), msCalentamiento(msCalentamiento), encendidoEn(0), nivelSuavizado(0) {
}

void SensorHumo::iniciar() {
    analogReadResolution(12);
    encendidoEn = millis();
    nivelSuavizado = analogRead(pinAdc);
}

int SensorHumo::leerNivel() {
    int crudo = analogRead(pinAdc);
    nivelSuavizado = (int) ((1.0f - PESO_NUEVA_LECTURA) * nivelSuavizado + PESO_NUEVA_LECTURA * crudo);
    return nivelSuavizado;
}

bool SensorHumo::estaCalentando() const {
    return (millis() - encendidoEn) < msCalentamiento;
}
