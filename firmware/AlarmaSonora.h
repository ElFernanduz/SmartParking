#ifndef ALARMA_SONORA_H
#define ALARMA_SONORA_H

#include <Arduino.h>

/** Buzzer activo controlado con una salida digital. */
class AlarmaSonora {
public:
    explicit AlarmaSonora(uint8_t pin);

    void iniciar();
    void activar();
    void silenciar();
    bool estaActiva() const;

private:
    uint8_t pin;
    bool activa;
};

#endif
