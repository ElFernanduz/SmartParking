#include "BarreraMotor.h"

static const int GRADOS_POR_PASO = 3;
static const unsigned long MS_ENTRE_PASOS = 15;

BarreraMotor::BarreraMotor(uint8_t pinSenal, int anguloCerrado, int anguloAbierto)
    : pinSenal(pinSenal), anguloCerrado(anguloCerrado), anguloAbierto(anguloAbierto),
      anguloActual(anguloCerrado), anguloObjetivo(anguloCerrado), ultimoPasoEn(0) {
}

void BarreraMotor::iniciar() {
    servo.setPeriodHertz(50);
    servo.attach(pinSenal, 500, 2400);
    anguloActual = anguloCerrado;
    anguloObjetivo = anguloCerrado;
    servo.write(anguloActual);
}

void BarreraMotor::abrir() {
    anguloObjetivo = anguloAbierto;
}

void BarreraMotor::cerrar() {
    anguloObjetivo = anguloCerrado;
}

void BarreraMotor::actualizar() {
    if (anguloActual == anguloObjetivo) {
        return;
    }
    if ((millis() - ultimoPasoEn) < MS_ENTRE_PASOS) {
        return;
    }
    ultimoPasoEn = millis();

    if (anguloActual < anguloObjetivo) {
        anguloActual = min(anguloActual + GRADOS_POR_PASO, anguloObjetivo);
    } else {
        anguloActual = max(anguloActual - GRADOS_POR_PASO, anguloObjetivo);
    }
    servo.write(anguloActual);
}

bool BarreraMotor::estaAbierta() const {
    return anguloObjetivo == anguloAbierto;
}

bool BarreraMotor::enMovimiento() const {
    return anguloActual != anguloObjetivo;
}
