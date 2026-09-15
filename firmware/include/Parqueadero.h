#ifndef PARQUEADERO_H
#define PARQUEADERO_H

// Representa el estado local (en el ESP32) del parqueadero.
// La fuente de verdad persistente vive en el backend (Spring Boot);
// esta clase es una caché en memoria que se sincroniza con la API.
class Parqueadero {
private:
    int capacidadMaxima;
    int espaciosDisponibles;

public:
    explicit Parqueadero(int capacidadMaxima)
        : capacidadMaxima(capacidadMaxima), espaciosDisponibles(capacidadMaxima) {}

    // cantidad puede ser -1 (entra un vehículo) o +1 (sale un vehículo)
    void actualizarEspacios(int cantidad) {
        espaciosDisponibles += cantidad;
        if (espaciosDisponibles < 0) espaciosDisponibles = 0;
        if (espaciosDisponibles > capacidadMaxima) espaciosDisponibles = capacidadMaxima;
    }

    int obtenerDisponibilidad() const {
        return espaciosDisponibles;
    }

    int getCapacidadMaxima() const { return capacidadMaxima; }
};

#endif
