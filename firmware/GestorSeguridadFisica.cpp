#include "GestorSeguridadFisica.h"

static const int UMBRAL_POR_DEFECTO = 400;

GestorSeguridadFisica::GestorSeguridadFisica(uint8_t pinAdcHumo, uint8_t pinBuzzer,
                                             unsigned long msCalentamiento,
                                             unsigned long msEntreLecturas)
    : sensorHumo(pinAdcHumo, msCalentamiento), alarma(pinBuzzer),
      msEntreLecturas(msEntreLecturas), ultimaLecturaEn(0), umbralLocal(UMBRAL_POR_DEFECTO) {
}

void GestorSeguridadFisica::iniciar() {
    sensorHumo.iniciar();
    alarma.iniciar();
}

int GestorSeguridadFisica::monitorear() {
    if ((millis() - ultimaLecturaEn) < msEntreLecturas) {
        return -1;
    }
    ultimaLecturaEn = millis();

    int nivel = sensorHumo.leerNivel();
    if (sensorHumo.estaCalentando()) {
        return -1;
    }
    return nivel;
}

void GestorSeguridadFisica::modoSeguro() {
    if (sensorHumo.estaCalentando()) {
        return;
    }
    int nivel = sensorHumo.leerNivel();
    if (nivel > umbralLocal) {
        alarma.activar();
    } else if (alarma.estaActiva()) {
        alarma.silenciar();
    }
}

void GestorSeguridadFisica::fijarUmbralLocal(int umbral) {
    umbralLocal = umbral;
}

void GestorSeguridadFisica::activarAlarma() {
    alarma.activar();
}

void GestorSeguridadFisica::silenciarAlarma() {
    alarma.silenciar();
}
