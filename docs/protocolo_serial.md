# Protocolo de comunicación serial

Contrato de comunicación entre el firmware del Arduino y la aplicación host. Es la frontera formal entre hardware y software.

## Formato

- Comunicación por líneas de texto terminadas en salto de línea (`\n`).
- Velocidad: 9600 o 115200 baudios (se fija al iniciar el firmware; debe coincidir en ambos extremos).
- Cada línea es un mensaje completo. El texto plano facilita depurar con el monitor serial y desacopla firmware y host.

## Mensajes del Arduino hacia el host (eventos y telemetría)

| Mensaje | Significado |
|---|---|
| `EVT:ENTRY_DETECTED` | Vehículo presente en la entrada. |
| `EVT:EXIT_DETECTED` | Vehículo presente en la salida. |
| `EVT:VEHICLE_PASSED:ENTRY` | El vehículo terminó de pasar por la entrada. |
| `EVT:VEHICLE_PASSED:EXIT` | El vehículo terminó de pasar por la salida. |
| `TEL:SMOKE:<n>` | Lectura analógica del sensor de humo (0 a 1023). |
| `EVT:READY` | El firmware terminó de iniciar y está listo. |
| `EVT:HEARTBEAT` | Señal periódica de vida del firmware. |

## Mensajes del host hacia el Arduino (comandos)

| Mensaje | Acción |
|---|---|
| `CMD:OPEN:ENTRY` | Abrir barrera de entrada. |
| `CMD:CLOSE:ENTRY` | Cerrar barrera de entrada. |
| `CMD:OPEN:EXIT` | Abrir barrera de salida. |
| `CMD:CLOSE:EXIT` | Cerrar barrera de salida. |
| `CMD:ALARM:ON` | Activar la alarma sonora. |
| `CMD:ALARM:OFF` | Silenciar la alarma. |
| `CMD:DISPLAY:<n>` | Mostrar el número de cupos en el display. |
| `CMD:CONFIG:THRESHOLD:<n>` | Fijar el umbral de humo en el firmware (para el modo seguro). |
| `CMD:PING` | Solicitar un heartbeat inmediato. |

## Reglas

- El host es la autoridad de negocio: decide cuándo abrir, cerrar o alarmar. El firmware ejecuta y reporta.
- El firmware nunca calcula cupos ni decide autorizaciones. Solo detecta, ejecuta y muestra lo que el host indica.
- El firmware confirma el fin del paso (`EVT:VEHICLE_PASSED`) solo cuando el sensor queda liberado de forma estable, tras un tiempo de antirrebote.
- Si el firmware no recibe comandos ni `CMD:PING` durante más de N segundos, entra en modo seguro: mantiene la entrada cerrada, permite la salida y sostiene la alarma si detecta humo.

## Ejemplo de secuencia (ingreso)

```
Arduino -> Host:  EVT:ENTRY_DETECTED
Host    -> Arduino: CMD:OPEN:ENTRY
Host    -> Arduino: CMD:DISPLAY:5
Arduino -> Host:  EVT:VEHICLE_PASSED:ENTRY
Host    -> Arduino: CMD:CLOSE:ENTRY
```
