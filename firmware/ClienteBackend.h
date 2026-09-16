#ifndef CLIENTE_BACKEND_H
#define CLIENTE_BACKEND_H

#include <Arduino.h>
#include <WebSocketsClient.h>

/** Conexion WebSocket con el backend: envia eventos y recibe comandos. */
class ClienteBackend {
public:
    typedef void (*ManejadorComando)(const String& json);

    ClienteBackend(const char* host, uint16_t puerto, const char* token);

    void conectar(ManejadorComando alRecibirComando);
    void actualizar();

    void enviarEvento(const String& json);

    bool estaConectado() const;

    /** Milisegundos desde la ultima vez que hubo enlace con el backend. */
    unsigned long msSinConexion() const;

private:
    WebSocketsClient socket;
    const char* host;
    uint16_t puerto;
    const char* token;
    bool conectado;
    unsigned long ultimaConexionEn;
};

#endif
