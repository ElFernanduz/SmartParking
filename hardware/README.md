# Hardware y montaje

Lista de materiales, asignación de pines y notas de conexión de la maqueta. Los diagramas y fotos van en la carpeta `diagramas/`.

## Lista de materiales

| Componente | Cantidad | Notas |
|---|---|---|
| Arduino Uno | 1 | Placa principal. |
| Sensor IR de obstáculo | 2 | Detección de vehículo en entrada y salida. |
| Sensor MQ-2 | 1 | Humo y gas. Salida analógica. |
| Servomotor (SG90 o similar) | 2 | Barreras de entrada y salida. |
| Buzzer | 1 | Alarma sonora. |
| Display de 7 segmentos | 1 | Indicador de cupos. |
| Fuente externa 5V | 1 | Alimentación de los servos. |
| Resistencias | varias | Una por segmento del display; según sensores. |
| Protoboard y cables | — | Montaje. |
| Registro 74HC595 | 0 a 2 (opcional) | Solo para display de dos dígitos. |

## Alimentación

Los servos generan picos de corriente que pueden reiniciar el Arduino si se alimentan desde el pin de 5V de la placa. Por eso los servos se alimentan desde la fuente externa de 5V. La tierra de la fuente externa debe unirse a la tierra del Arduino (tierra común); de lo contrario los servos no responden bien.

## Asignación de pines (por defecto: display de un dígito, hasta 9 cupos)

Los pines 0 y 1 se reservan para la comunicación USB con el host.

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

Con un solo dígito conectado directo no hay multiplexación, así que no hay parpadeo ni conflicto de temporización con los servos. Los pines A1 a A5 quedan libres.

Nota sobre los pines 9 y 10: la librería Servo deshabilita la salida PWM en esos pines, pero la escritura digital sigue funcionando. Aquí se usan como salidas digitales para segmentos del display, así que no hay conflicto.

## Ampliación a dos dígitos (hasta 99 cupos)

Si la maqueta tendrá 10 o más cupos, se necesita un display de dos dígitos. La opción limpia es usar registros de desplazamiento 74HC595 en cadena (uno por dígito), que controlan ambos dígitos de forma estática con solo tres pines (datos, reloj y latch). Esto evita la multiplexación por software y libera pines. En ese caso, los pines de los segmentos de la tabla anterior se reemplazan por los tres del 74HC595 y los demás componentes conservan su conexión.

Recomendación: definir cuántos cupos tendrá la maqueta antes del montaje. Con 9 o menos no se requieren piezas adicionales.
