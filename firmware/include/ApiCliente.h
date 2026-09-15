#ifndef API_CLIENTE_H
#define API_CLIENTE_H

#include <WiFi.h>
#include <HTTPClient.h>

// Encapsula las llamadas HTTP del ESP32 hacia el backend Spring Boot.
// Endpoints esperados (ver backend/.../ParqueaderoController.java):
//   GET  /api/parqueadero            -> disponibilidad actual
//   POST /api/parqueadero/actualizar -> reporta un cambio de espacios
class ApiCliente {
private:
    String baseUrl;

public:
    explicit ApiCliente(const String &baseUrl) : baseUrl(baseUrl) {}

    int obtenerDisponibilidadRemota() {
        HTTPClient http;
        http.begin(baseUrl + "/api/parqueadero");
        int codigo = http.GET();
        int disponibles = -1;
        if (codigo == 200) {
            String payload = http.getString();
            int idx = payload.indexOf("espaciosDisponibles");
            if (idx >= 0) {
                disponibles = payload.substring(payload.indexOf(':', idx) + 1).toInt();
            }
        }
        http.end();
        return disponibles;
    }

    bool reportarCambioEspacios(int cantidad) {
        HTTPClient http;
        http.begin(baseUrl + "/api/parqueadero/actualizar");
        http.addHeader("Content-Type", "application/json");
        String body = "{\"cantidad\":" + String(cantidad) + "}";
        int codigo = http.POST(body);
        http.end();
        return codigo == 200;
    }
};

#endif
