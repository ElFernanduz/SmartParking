#include <Arduino.h>
#include "SensorHumoGas.h"
#include "SensorProximidad.h"
#include "AlarmaBuzzer.h"
#include "BarreraAcceso.h"
#include "DisplayContador.h"
#include "ControlEmergencia.h"
#include "ControlAcceso.h"
#include "Parqueadero.h"
#include "ApiCliente.h"
#include "SistemaControl.h"

// --- Configuración de pines (ajustar según el montaje físico) ---
const int PIN_SENSOR_HUMO = 34;
const int PIN_SENSOR_PROXIMIDAD = 35;
const int PIN_BUZZER = 25;
const int PIN_SERVO_BARRERA = 27;
const int CAPACIDAD_MAXIMA = 20;
const char *WIFI_SSID = "TU_RED";
const char *WIFI_PASSWORD = "TU_PASSWORD";
const char *BACKEND_URL = "http://TU_SERVIDOR:8080";

// --- Instancias de los componentes del diagrama de clases ---
SensorHumoGas sensorHumoGas("SHG-01", PIN_SENSOR_HUMO);
SensorProximidad sensorProximidad("SP-01", PIN_SENSOR_PROXIMIDAD);
AlarmaBuzzer alarmaBuzzer(PIN_BUZZER);
BarreraAcceso barreraAcceso(PIN_SERVO_BARRERA);
DisplayContador displayContador(CAPACIDAD_MAXIMA);
Parqueadero parqueadero(CAPACIDAD_MAXIMA);
ApiCliente apiCliente(BACKEND_URL);

ControlEmergencia controlEmergencia(sensorHumoGas, alarmaBuzzer);
ControlAcceso controlAcceso(sensorProximidad, barreraAcceso, displayContador);

SistemaControl sistemaControl(controlEmergencia, controlAcceso, parqueadero,
                               displayContador, apiCliente);

void conectarWifi() {
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
    }
}

void setup() {
    Serial.begin(115200);
    pinMode(PIN_BUZZER, OUTPUT);
    barreraAcceso.iniciar();

    conectarWifi();
    sistemaControl.iniciarSistema();
}

void loop() {
    sistemaControl.actualizar();
    delay(200);
}
