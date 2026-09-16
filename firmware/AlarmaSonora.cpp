#include "AlarmaSonora.h"

AlarmaSonora::AlarmaSonora(uint8_t pin) : pin(pin), activa(false) {
}

void AlarmaSonora::iniciar() {
    pinMode(pin, OUTPUT);
    digitalWrite(pin, LOW);
    activa = false;
}

void AlarmaSonora::activar() {
    digitalWrite(pin, HIGH);
    activa = true;
}

void AlarmaSonora::silenciar() {
    digitalWrite(pin, LOW);
    activa = false;
}

bool AlarmaSonora::estaActiva() const {
    return activa;
}
