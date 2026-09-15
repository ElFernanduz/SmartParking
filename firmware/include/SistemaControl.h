#ifndef SISTEMA_CONTROL_H
#define SISTEMA_CONTROL_H

#include "ControlEmergencia.h"
#include "ControlAcceso.h"
#include "Parqueadero.h"
#include "DisplayContador.h"
#include "ApiCliente.h"

// Compuesto por ControlEmergencia y ControlAcceso.
// Administra Parqueadero y actualiza el DisplayContador.
class SistemaControl {
private:
    ControlEmergencia &controlEmergencia;
    ControlAcceso &controlAcceso;
    Parqueadero &parqueadero;
    DisplayContador &displayContador;
    ApiCliente &apiCliente;
    bool sistemaActivo;

public:
    SistemaControl(ControlEmergencia &controlEmergencia, ControlAcceso &controlAcceso,
                    Parqueadero &parqueadero, DisplayContador &displayContador,
                    ApiCliente &apiCliente)
        : controlEmergencia(controlEmergencia), controlAcceso(controlAcceso),
          parqueadero(parqueadero), displayContador(displayContador),
          apiCliente(apiCliente), sistemaActivo(false) {}

    void iniciarSistema() {
        sistemaActivo = true;
        displayContador.actualizarContador(parqueadero.obtenerDisponibilidad());
    }

    void detenerSistema() {
        sistemaActivo = false;
    }

    // Se llama en cada ciclo del loop() de Arduino.
    void actualizar() {
        if (!sistemaActivo) return;

        controlEmergencia.monitorearNiveles();

        bool vehiculoDetectado = controlAcceso.verificarProximidad();
        controlAcceso.gestionarIngreso();

        if (vehiculoDetectado) {
            parqueadero.actualizarEspacios(-1);
            displayContador.actualizarContador(parqueadero.obtenerDisponibilidad());
            apiCliente.reportarCambioEspacios(-1);
        }
    }
};

#endif
