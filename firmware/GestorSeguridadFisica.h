#ifndef GESTOR_SEGURIDAD_FISICA_H
#define GESTOR_SEGURIDAD_FISICA_H

#include <Arduino.h>
#include "AlarmaSonora.h"
#include "SensorHumo.h"

/**
 * Humo y alarma. El umbral lo evalua el backend; aqui solo se guarda la
 * ultima copia recibida para poder reaccionar en modo seguro.
 */
class GestorSeguridadFisica {
public:
    GestorSeguridadFisica(uint8_t pinAdcHumo, uint8_t pinBuzzer,
                          unsigned long msCalentamiento, unsigned long msEntreLecturas);

    void iniciar();

    /**
     * Devuelve el nivel a enviar como telemetria, o -1 si no toca enviar
     * todavia o el sensor sigue calentando.
     */
    int monitorear();

    /** Sin backend, el firmware decide solo con la ultima copia del umbral. */
    void modoSeguro();

    void fijarUmbralLocal(int umbral);
    void activarAlarma();
    void silenciarAlarma();

private:
    SensorHumo sensorHumo;
    AlarmaSonora alarma;
    unsigned long msEntreLecturas;
    unsigned long ultimaLecturaEn;
    int umbralLocal;
};

#endif
