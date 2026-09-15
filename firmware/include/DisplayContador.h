#ifndef DISPLAY_CONTADOR_H
#define DISPLAY_CONTADOR_H

#include <Arduino.h>

// Controla el display de 7 segmentos con el conteo de espacios disponibles.
class DisplayContador {
private:
    int espaciosActuales;

public:
    explicit DisplayContador(int espaciosIniciales = 0)
        : espaciosActuales(espaciosIniciales) {}

    void actualizarContador(int espacios) {
        espaciosActuales = espacios;
        mostrarValor();
    }

    void mostrarValor() {
        // TODO: mapear espaciosActuales a los segmentos físicos (A-G)
    }

    bool hayEspacioDisponible() {
        return espaciosActuales != 0;
    }

    int getEspaciosActuales() const { return espaciosActuales; }
};

#endif
