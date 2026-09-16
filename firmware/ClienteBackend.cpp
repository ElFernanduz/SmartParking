#include "ClienteBackend.h"

// El cliente WebSocket entrega los eventos por callback estatico, asi que la
// instancia activa y el manejador de comandos se guardan a nivel de archivo.
static ClienteBackend* instancia = nullptr;
static ClienteBackend::ManejadorComando manejadorComando = nullptr;
static bool* banderaConectado = nullptr;
static unsigned long* marcaUltimaConexion = nullptr;

static void alOcurrirEvento(WStype_t tipo, uint8_t* carga, size_t longitud) {
    switch (tipo) {
        case WStype_CONNECTED:
            *banderaConectado = true;
            *marcaUltimaConexion = millis();
            Serial.println(F("[backend] conectado"));
            break;
        case WStype_DISCONNECTED:
            *banderaConectado = false;
            Serial.println(F("[backend] desconectado"));
            break;
        case WStype_TEXT: {
            *marcaUltimaConexion = millis();
            String json;
            json.reserve(longitud + 1);
            for (size_t i = 0; i < longitud; i++) {
                json += (char) carga[i];
            }
            if (manejadorComando != nullptr) {
                manejadorComando(json);
            }
            break;
        }
        default:
            break;
    }
}

ClienteBackend::ClienteBackend(const char* host, uint16_t puerto, const char* token)
    : host(host), puerto(puerto), token(token), conectado(false), ultimaConexionEn(0) {
}

void ClienteBackend::conectar(ManejadorComando alRecibirComando) {
    instancia = this;
    manejadorComando = alRecibirComando;
    banderaConectado = &conectado;
    marcaUltimaConexion = &ultimaConexionEn;
    ultimaConexionEn = millis();

    String ruta = String("/dispositivo?token=") + token;
    socket.begin(host, puerto, ruta);
    socket.onEvent(alOcurrirEvento);
    socket.setReconnectInterval(3000);
}

void ClienteBackend::actualizar() {
    socket.loop();
}

void ClienteBackend::enviarEvento(const String& json) {
    if (conectado) {
        String carga = json;   // sendTXT pide una referencia no constante
        socket.sendTXT(carga);
    }
}

bool ClienteBackend::estaConectado() const {
    return conectado;
}

unsigned long ClienteBackend::msSinConexion() const {
    return conectado ? 0 : (millis() - ultimaConexionEn);
}
