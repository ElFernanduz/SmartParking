# Host (aplicación de control)

Aplicación en Java que contiene la lógica del sistema, se comunica con el Arduino por puerto serial y sirve el panel web. Proyecto Maven con arquitectura hexagonal.

## Estructura de paquetes

```
com.unillanos.smartparking
├── Main.java                  Composition root: crea y conecta todos los objetos
├── domain
│   ├── model                  ParkingLot, Spot, Visit, SecurityEvent, VehicleId
│   ├── state                  Estados de barrera y de sistema
│   ├── policy                 Politicas de acceso (Strategy)
│   ├── event                  Eventos de dominio
│   └── port
│       ├── in                 Interfaces de casos de uso
│       └── out                ArduinoGatewayPort, repositorios, publicador de eventos
├── application
│   ├── usecase                Implementaciones de los casos de uso
│   └── command                Objetos Command de control
├── infrastructure
│   ├── serial                 Adaptador serial con jSerialComm y parser del protocolo
│   ├── persistence            Repositorios JDBC sobre SQLite
│   ├── config                 Carga de configuracion
│   └── logging                Configuracion de logs
└── interfaces
    └── web                    Controladores Javalin, WebSocket y archivos estaticos
```

Los recursos están en `src/main/resources`: el esquema de la base de datos en `db/schema.sql`, la configuración de logs en `logback.xml` y los archivos del dashboard en `static/`.

## Requisitos

- JDK 17 o superior
- Maven 3.9 o superior

## Compilación y ejecución

```bash
mvn clean package
java -jar target/smart-parking-lot.jar
```

El panel queda disponible en `http://localhost:7070` (puerto configurable).

## Pruebas

```bash
mvn test
```

El núcleo (dominio y aplicación) se prueba sin hardware, usando un adaptador serial falso y repositorios en memoria. Esto permite validar el conteo, las transiciones de estado y el protocolo de emergencia sin la maqueta.

## Nota

El proyecto está en desarrollo. Esta estructura define las carpetas y la configuración base; la implementación se agrega según el plan de trabajo.
