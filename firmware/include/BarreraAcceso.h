#ifndef BARRERA_ACCESO_H
#define BARRERA_ACCESO_H

#include "Actuador.h"
#include <Servo.h>

class BarreraAcceso : public Actuador {
private:
    Servo servo;
    int pin;
    int anguloAbierto;
    int anguloCerrado;

public:
    BarreraAcceso(int pin, int anguloAbierto = 90, int anguloCerrado = 0)
        : pin(pin), anguloAbierto(anguloAbierto), anguloCerrado(anguloCerrado) {}

    void iniciar() {
        servo.attach(pin);
        cerrar();
    }

    void activar() override { abrir(); }
    void desactivar() override { cerrar(); }

    void abrir() {
        estado = true;
        servo.write(anguloAbierto);
    }

    void cerrar() {
        estado = false;
        servo.write(anguloCerrado);
    }
};

#endif
