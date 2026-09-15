#ifndef ACTUADOR_H
#define ACTUADOR_H

#include <Arduino.h>

// Clase abstracta base para todos los actuadores del sistema.
class Actuador {
protected:
    bool estado;

public:
    Actuador() : estado(false) {}
    virtual ~Actuador() {}

    virtual void activar() = 0;
    virtual void desactivar() = 0;

    bool getEstado() const { return estado; }
};

#endif
