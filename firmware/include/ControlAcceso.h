#ifndef CONTROL_ACCESO_H
#define CONTROL_ACCESO_H

#include "SensorProximidad.h"
#include "BarreraAcceso.h"
#include "DisplayContador.h"

// Lee datos del sensor de proximidad, consulta el DisplayContador
// y controla el actuador BarreraAcceso.
class ControlAcceso {
private:
    SensorProximidad &sensorProximidad;
    BarreraAcceso &barreraAcceso;
    DisplayContador &displayContador;

public:
    ControlAcceso(SensorProximidad &sensorProximidad, BarreraAcceso &barreraAcceso,
                   DisplayContador &displayContador)
        : sensorProximidad(sensorProximidad), barreraAcceso(barreraAcceso),
          displayContador(displayContador) {}

    bool verificarProximidad() {
        sensorProximidad.leerDatos();
        return sensorProximidad.detectarVehiculo();
    }

    void gestionarIngreso() {
        if (!verificarProximidad()) {
            barreraAcceso.cerrar();
            return;
        }
        // La barrera solo se abre si el contador tiene espacio disponible.
        if (displayContador.hayEspacioDisponible()) {
            barreraAcceso.abrir();
        } else {
            barreraAcceso.cerrar();
        }
    }
};

#endif
