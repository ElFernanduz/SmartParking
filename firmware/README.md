# Firmware (Arduino)

Código del Arduino Uno que lee los sensores, controla los actuadores y se comunica con el host mediante el protocolo serial documentado en [../docs/protocolo_serial.md](../docs/protocolo_serial.md).

## Responsabilidades

- Leer los dos sensores IR (entrada y salida) con antirrebote, para no generar eventos duplicados.
- Leer el sensor MQ-2 por entrada analógica y enviarlo periódicamente como telemetría.
- Mover los servos de las barreras al recibir comandos.
- Encender o apagar el buzzer al recibir comandos.
- Actualizar el display de 7 segmentos con el número que envíe el host.
- Enviar heartbeat periódico y aplicar el modo seguro ante ausencia de host.

## Principio de diseño

El firmware solo detecta, ejecuta y muestra. Toda la lógica de negocio (conteo, autorización, protocolo de emergencia) vive en el host. Esto garantiza que el conteo sea consistente y persistente.

## Asignación de pines (configuración por defecto)

Un solo dígito de display conectado directo, para una capacidad de hasta 9 cupos. Los pines 0 y 1 se reservan para la comunicación USB.

| Pin | Conexión |
|---|---|
| D2 | Sensor IR entrada |
| D3 | Sensor IR salida |
| D4 | Buzzer |
| D5 | Servo barrera entrada |
| D6 | Servo barrera salida |
| D7 | Display segmento a |
| D8 | Display segmento b |
| D9 | Display segmento c |
| D10 | Display segmento d |
| D11 | Display segmento e |
| D12 | Display segmento f |
| D13 | Display segmento g |
| A0 | Sensor MQ-2 (analógico) |

Detalles de montaje, alimentación y la alternativa de display de dos dígitos en [../hardware/README.md](../hardware/README.md).

## Notas

- El MQ-2 requiere tiempo de precalentamiento tras energizar antes de dar lecturas estables. El firmware espera y anuncia `EVT:READY`.
- El firmware es no bloqueante: no usa esperas largas; se apoya en `millis()` para muestreo, heartbeat y antirrebote.
- El umbral de humo se aplica en el host, pero el firmware guarda la última copia recibida para el modo seguro.

## Cómo cargar

1. Abrir el proyecto en el Arduino IDE o PlatformIO.
2. Seleccionar la placa Arduino Uno y el puerto correspondiente.
3. Compilar y cargar.
4. Cerrar el monitor serial antes de ejecutar el host, ya que el host toma el puerto.
