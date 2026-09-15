#ifndef SENSOR_H
#define SENSOR_H

#include <Arduino.h>

// Clase abstracta base para todos los sensores del sistema.
class Sensor {
protected:
    String id;
    bool activo;

public:
    explicit Sensor(const String &id) : id(id), activo(false) {}
    virtual ~Sensor() {}

    virtual void encender() { activo = true; }
    virtual void apagar()   { activo = false; }

    bool estaActivo() const { return activo; }
    String getId() const { return id; }

    // Cada sensor concreto define cómo lee su dato físico.
    virtual float leerDatos() = 0;
};

#endif
