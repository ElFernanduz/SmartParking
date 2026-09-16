/*
 * Smart Parking Lot - Firmware de la ESP32-C3 Super Mini.
 *
 * El firmware solo detecta, ejecuta y muestra: toda la logica de negocio
 * vive en el backend. Lo unico que decide localmente es la condicion segura
 * de las barreras y el modo seguro cuando se pierde el enlace.
 *
 * Placa en el IDE: ESP32C3 Dev Module, con USB CDC habilitado.
 * Librerias: WiFi (nucleo ESP32), arduinoWebSockets, ArduinoJson y ESP32Servo.
 */

#include <Arduino.h>
#include <ArduinoJson.h>
#include <WiFi.h>

#include "ClienteBackend.h"
#include "ControladorAccesoFisico.h"
#include "GestorSeguridadFisica.h"

// ---------------------------------------------------------------------------
// Configuracion de la red y del backend. Ajustar antes de cargar el firmware.
// ---------------------------------------------------------------------------
static const char* WIFI_SSID = "CAMBIAR_SSID";
static const char* WIFI_PASSWORD = "CAMBIAR_PASSWORD";
static const char* BACKEND_HOST = "192.168.1.100";   // IP del PC con el backend
static const uint16_t BACKEND_PUERTO = 7070;
static const char* DISPOSITIVO_TOKEN = "cambia-este-token";

// ---------------------------------------------------------------------------
// Asignacion de pines (seccion 19.3 de la especificacion).
// Se evitan GPIO2, GPIO8 y GPIO9 porque intervienen en el arranque.
// ---------------------------------------------------------------------------
static const uint8_t PIN_MQ2 = 0;      // ADC1, con divisor de voltaje a 3.3V
static const uint8_t PIN_IR_ENTRADA = 5;
static const uint8_t PIN_IR_SALIDA = 6;
static const uint8_t PIN_SERVO_ENTRADA = 7;
static const uint8_t PIN_SERVO_SALIDA = 10;
static const uint8_t PIN_BUZZER = 4;

// ---------------------------------------------------------------------------
// Tiempos y angulos.
// ---------------------------------------------------------------------------
static const unsigned long MS_ANTIRREBOTE_IR = 80;
static const unsigned long MS_CALENTAMIENTO_MQ2 = 90000;   // dentro de los 60 a 120 s
static const unsigned long MS_ENTRE_TELEMETRIAS = 2000;
static const unsigned long MS_ENTRE_LATIDOS = 2000;
static const unsigned long MS_PARA_MODO_SEGURO = 8000;

static const int ANGULO_CERRADO = 0;
static const int ANGULO_ABIERTO = 90;

ControladorAccesoFisico accesoEntrada("ENTRADA", PIN_IR_ENTRADA, PIN_SERVO_ENTRADA,
                                      MS_ANTIRREBOTE_IR, ANGULO_CERRADO, ANGULO_ABIERTO);
ControladorAccesoFisico accesoSalida("SALIDA", PIN_IR_SALIDA, PIN_SERVO_SALIDA,
                                     MS_ANTIRREBOTE_IR, ANGULO_CERRADO, ANGULO_ABIERTO);
GestorSeguridadFisica seguridad(PIN_MQ2, PIN_BUZZER, MS_CALENTAMIENTO_MQ2, MS_ENTRE_TELEMETRIAS);
ClienteBackend backend(BACKEND_HOST, BACKEND_PUERTO, DISPOSITIVO_TOKEN);

static unsigned long ultimoLatidoEn = 0;
static bool enModoSeguro = false;
static bool presentadoAlBackend = false;

// ---------------------------------------------------------------------------
// Envio de mensajes del protocolo hacia el backend.
// ---------------------------------------------------------------------------

static void enviarSimple(const char* tipo) {
    JsonDocument documento;
    documento["tipo"] = tipo;
    String salida;
    serializeJson(documento, salida);
    backend.enviarEvento(salida);
}

static void enviarConPunto(const char* tipo, const char* punto) {
    JsonDocument documento;
    documento["tipo"] = tipo;
    documento["punto"] = punto;
    String salida;
    serializeJson(documento, salida);
    backend.enviarEvento(salida);
}

static void enviarTelemetriaHumo(int nivel) {
    JsonDocument documento;
    documento["tipo"] = "TELEMETRIA_HUMO";
    documento["nivel"] = nivel;
    String salida;
    serializeJson(documento, salida);
    backend.enviarEvento(salida);
}

// ---------------------------------------------------------------------------
// Comandos que llegan del backend.
// ---------------------------------------------------------------------------

static ControladorAccesoFisico* accesoDe(const char* punto) {
    if (strcmp(punto, "ENTRADA") == 0) {
        return &accesoEntrada;
    }
    if (strcmp(punto, "SALIDA") == 0) {
        return &accesoSalida;
    }
    return nullptr;
}

static void alRecibirComando(const String& json) {
    JsonDocument documento;
    if (deserializeJson(documento, json)) {
        Serial.println(F("[backend] comando ilegible"));
        return;
    }

    const char* tipo = documento["tipo"] | "";

    if (strcmp(tipo, "ABRIR") == 0) {
        ControladorAccesoFisico* acceso = accesoDe(documento["punto"] | "");
        if (acceso != nullptr) {
            acceso->ejecutarApertura();
        }
    } else if (strcmp(tipo, "CERRAR") == 0) {
        ControladorAccesoFisico* acceso = accesoDe(documento["punto"] | "");
        if (acceso != nullptr) {
            acceso->ejecutarCierre();
        }
    } else if (strcmp(tipo, "ALARMA") == 0) {
        if (strcmp(documento["estado"] | "", "ON") == 0) {
            seguridad.activarAlarma();
        } else {
            seguridad.silenciarAlarma();
        }
    } else if (strcmp(tipo, "CONFIG_UMBRAL") == 0) {
        seguridad.fijarUmbralLocal(documento["umbral"] | 400);
    } else if (strcmp(tipo, "PANTALLA") == 0) {
        // Sin display fisico el dato solo se deja en el monitor serie.
        Serial.printf("[cupos] %d\n", documento["cupos"] | 0);
    } else if (strcmp(tipo, "PING") == 0) {
        enviarSimple("LATIDO");
    }
}

// ---------------------------------------------------------------------------
// Ciclo de un punto de acceso.
// ---------------------------------------------------------------------------

static void atenderAcceso(ControladorAccesoFisico& acceso, const char* tipoDeteccion) {
    EventoAcceso evento = acceso.actualizar();
    switch (evento) {
        case EventoAcceso::PRESENCIA_DETECTADA:
            enviarSimple(tipoDeteccion);
            break;
        case EventoAcceso::PASO_COMPLETADO:
            enviarConPunto("PASO_COMPLETADO", acceso.nombrePunto());
            break;
        case EventoAcceso::CIERRE_BLOQUEADO:
            enviarConPunto("BARRERA_BLOQUEADA", acceso.nombrePunto());
            break;
        case EventoAcceso::NINGUNO:
        default:
            break;
    }
}

// ---------------------------------------------------------------------------
// Modo seguro: sin backend, la entrada queda cerrada, la salida se permite y
// la alarma suena si el humo supera la ultima copia del umbral.
// ---------------------------------------------------------------------------

static void aplicarModoSeguro() {
    if (!enModoSeguro) {
        enModoSeguro = true;
        Serial.println(F("[modo seguro] sin backend: entrada cerrada, salida permitida"));
        accesoEntrada.ejecutarCierre();
    }
    seguridad.modoSeguro();
    accesoEntrada.actualizar();

    // La salida sigue disponible: sin backend, la barrera la gobierna el firmware.
    EventoAcceso evento = accesoSalida.actualizar();
    if (evento == EventoAcceso::PRESENCIA_DETECTADA) {
        accesoSalida.ejecutarApertura();
    } else if (evento == EventoAcceso::PASO_COMPLETADO) {
        accesoSalida.ejecutarCierre();
    }
}

static void conectarWiFi() {
    Serial.printf("[wifi] conectando a %s", WIFI_SSID);
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print('.');
    }
    Serial.printf("\n[wifi] conectado, IP %s\n", WiFi.localIP().toString().c_str());
}

void setup() {
    Serial.begin(115200);
    delay(300);
    Serial.println(F("\nSmart Parking Lot - firmware ESP32-C3"));

    // El control de acceso arranca de una; el MQ-2 calienta en segundo plano.
    accesoEntrada.iniciar();
    accesoSalida.iniciar();
    seguridad.iniciar();

    conectarWiFi();
    backend.conectar(alRecibirComando);
}

void loop() {
    backend.actualizar();

    if (!backend.estaConectado()) {
        presentadoAlBackend = false;
        if (backend.msSinConexion() > MS_PARA_MODO_SEGURO) {
            aplicarModoSeguro();
            return;
        }
    }

    // El LISTO se manda en cada enlace nuevo: el backend responde con el
    // umbral vigente y los cupos actuales.
    if (backend.estaConectado() && !presentadoAlBackend) {
        presentadoAlBackend = true;
        enModoSeguro = false;
        enviarSimple("LISTO");
    }

    atenderAcceso(accesoEntrada, "ENTRADA_DETECTADA");
    atenderAcceso(accesoSalida, "SALIDA_DETECTADA");

    int nivelHumo = seguridad.monitorear();
    if (nivelHumo >= 0) {
        enviarTelemetriaHumo(nivelHumo);
    }

    if ((millis() - ultimoLatidoEn) >= MS_ENTRE_LATIDOS) {
        ultimoLatidoEn = millis();
        enviarSimple("LATIDO");
    }
}
