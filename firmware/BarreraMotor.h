#ifndef BARRERA_MOTOR_H
#define BARRERA_MOTOR_H

#include <Arduino.h>
#include <ESP32Servo.h>

/**
 * Servo SG90 de una barrera. El movimiento es gradual y no bloqueante: cada
 * llamada a actualizar() avanza un paso hacia el angulo objetivo.
 */
class BarreraMotor {
public:
    BarreraMotor(uint8_t pinSenal, int anguloCerrado, int anguloAbierto);

    void iniciar();
    void abrir();
    void cerrar();

    /** Avanza el movimiento; se llama en cada ciclo del bucle principal. */
    void actualizar();

    bool estaAbierta() const;
    bool enMovimiento() const;

private:
    Servo servo;
    uint8_t pinSenal;
    int anguloCerrado;
    int anguloAbierto;
    int anguloActual;
    int anguloObjetivo;
    unsigned long ultimoPasoEn;
};

#endif
