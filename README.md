# Smart Parking Lot

Sistema de gestión automatizada de un parqueadero, construido como maqueta a escala. Un dispositivo ESP32-C3 con sensores y actuadores controla el acceso, y un backend en Java con arquitectura en capas mantiene el conteo, la seguridad y el historial, con un panel web en tiempo real.

![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?logo=springboot&logoColor=white)
![ESP32](https://img.shields.io/badge/ESP32--C3-WiFi-E7352C?logo=espressif&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-3-003B57?logo=sqlite&logoColor=white)
![Licencia](https://img.shields.io/badge/licencia-MIT-green)
![Estado](https://img.shields.io/badge/estado-en%20desarrollo-yellow)

## Descripción

El sistema controla el acceso vehicular de forma automática, mantiene el conteo de cupos disponibles en tiempo real y vigila la presencia de humo o gas, activando una alarma cuando se supera un umbral configurable. La ESP32-C3 se comunica con el backend por WiFi mediante WebSocket; el backend concentra toda la lógica de negocio y sirve un panel web de control y monitoreo. El acceso al panel se controla con usuarios, roles y permisos. Cada acceso y cada alerta quedan registrados de forma persistente.

## Características

- Detección automática de vehículos en entrada y salida.
- Control automático de barreras con cierre seguro.
- Conteo de cupos en tiempo real, único y persistente.
- Monitoreo continuo de humo con alarma y protocolo de emergencia.
- Panel web de control y monitoreo con actualización en vivo.
- Registro histórico de accesos y de eventos de seguridad.
- Capacidad y umbral configurables sin recompilar.
- Control de acceso al panel por usuarios, roles y permisos.

## Arquitectura

Arquitectura en capas. La capa web recibe la petición y delega en la de servicio; la de servicio aplica las reglas de negocio y pide datos a la de repositorio; la de repositorio habla con SQLite. Las dependencias van siempre hacia abajo. La lógica de negocio reside en el backend; la ESP32 solo detecta, ejecuta y muestra.

```mermaid
flowchart TB
    subgraph DISP["ESP32-C3"]
        FW["Firmware C++"]
    end
    subgraph HOST["Backend Spring Boot"]
        WEB["Presentacion: controladores y WebSocket"]
        SRV["Negocio: servicios"]
        REP["Persistencia: repositorios"]
    end
    DB[("SQLite")]
    UI["Dashboard web"]

    FW <-->|WiFi, WebSocket| WEB
    UI <-->|REST y WebSocket| WEB
    WEB --> SRV
    SRV --> REP
    REP --> DB
```

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 25, Spring Boot 4, Maven |
| Tiempo real y enlace con el dispositivo | Spring WebSocket |
| Persistencia | Spring Data JPA sobre SQLite |
| Frontend | HTML, CSS y JavaScript con Chart.js |
| Dispositivo | ESP32-C3 Super Mini (WiFi) |
| Firmware | C++ con el núcleo Arduino de ESP32 |

## Estructura del repositorio

```
smart-parking-lot/
├── docs/            Documentacion tecnica
├── firmware/        Firmware de la ESP32-C3 (C++)
├── hardware/        Lista de materiales y diagramas de conexion
└── host/            Backend en Java (proyecto Maven Spring Boot)
```

## Requisitos previos

- JDK 25 o superior
- Maven 3.9 o superior
- IDE de Arduino con soporte para ESP32 (o PlatformIO)
- Una ESP32-C3 con los sensores y actuadores descritos en `hardware/`

## Compilación y ejecución

Desde `host/`:

```bash
mvn clean package
java -jar target/smart-parking-lot.jar
```

El panel queda disponible en `http://localhost:7070`. La ESP32 debe estar en la misma red WiFi y apuntar a la IP del backend.

## Equipo

| Integrante | Código |
|---|---|
| Luis Miguel Bahamón González | 160005101 |
| Emerson Andrey Beltrán Álvarez | 160005103 |
| Juan Fernando Fernández Vega | 160005112 |
| Mario Alejandro Rey Reina | 160004833 |
| César Mauricio Pérez Santos | 160005130 |

Universidad de los Llanos — Ingeniería de Sistemas — Tecnologías Avanzadas.

## Licencia

Distribuido bajo la licencia MIT. Ver el archivo `LICENSE`.
