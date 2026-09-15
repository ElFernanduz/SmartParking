#ifndef ALARMA_BUZZER_H
#define ALARMA_BUZZER_H

#include "Actuador.h"

class AlarmaBuzzer : public Actuador {
private:
    int pin;

public:
    explicit AlarmaBuzzer(int pin) : pin(pin) {}

    void activar() override {
        estado = true;
        emitirSonido();
    }

    void desactivar() override {
        estado = false;
        silenciar();
    }

    void emitirSonido() {
        digitalWrite(pin, HIGH);
    }

    void silenciar() {
        digitalWrite(pin, LOW);
    }
};

#endif
