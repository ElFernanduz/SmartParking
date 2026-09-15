# Arquitectura SmartParking

El diagrama de clases original se dividió en dos subsistemas que se comunican por HTTP:

## Firmware (C++ / ESP32 - PlatformIO)
Control en tiempo real de sensores y actuadores:
- `Sensor` (abstracta), `SensorHumoGas`, `SensorProximidad`
- `Actuador` (abstracta), `AlarmaBuzzer`, `BarreraAcceso`
- `DisplayContador`
- `ControlEmergencia`, `ControlAcceso`
- `Parqueadero` (caché local en memoria)
- `SistemaControl` (compone y orquesta todo lo anterior)
- `ApiCliente` (nuevo: encapsula la sincronización HTTP con el backend)

## Backend (Java / Spring Boot)
Fuente de verdad persistente y API REST:
- `Parqueadero` (entidad JPA)
- `ParqueaderoRepository`
- `ParqueaderoService`
- `ParqueaderoController` → `GET /api/parqueadero`, `POST /api/parqueadero/actualizar`

## Flujo
1. `ControlAcceso` detecta un vehículo con `SensorProximidad`.
2. `SistemaControl` actualiza el `Parqueadero` local y el `DisplayContador`.
3. `ApiCliente` reporta el cambio al backend (`POST /api/parqueadero/actualizar`).
4. El backend persiste el nuevo conteo y lo expone vía `GET /api/parqueadero` para
   cualquier cliente adicional (dashboard web, app móvil, etc.).
