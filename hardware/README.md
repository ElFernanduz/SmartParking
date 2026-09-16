# Hardware y montaje

Maqueta del parqueadero: una ESP32-C3 Super Mini con dos puntos de acceso
controlados por servos, dos sensores infrarrojos de obstaculo, un sensor de
humo MQ-2 y un buzzer.

## Lista de materiales

| Componente | Cantidad |
|---|---|
| ESP32-C3 Super Mini | 1 |
| Sensor infrarrojo de obstaculo | 2 |
| Sensor MQ-2 | 1 |
| Servomotor SG90 | 2 |
| Buzzer activo de tres pines | 1 |
| Fuente externa de 5V y al menos 2A para los servos | 1 |

Elementos adicionales: resistencias para el divisor de voltaje del MQ-2,
protoboard y cables. El display de cupos es opcional; si se quiere uno
fisico, conviene un modulo I2C de dos pines (TM1637 o una pantalla I2C) en
lugar de un display de siete segmentos suelto, por la cantidad de pines de
la placa.

## Asignacion de pines

| Pin | Conexion |
|---|---|
| GPIO0 | MQ-2 salida analogica (ADC1, con divisor de voltaje) |
| GPIO5 | Sensor IR entrada |
| GPIO6 | Sensor IR salida |
| GPIO7 | Servo barrera entrada (senal) |
| GPIO10 | Servo barrera salida (senal) |
| GPIO4 | Buzzer |

Se evitan GPIO2, GPIO8 y GPIO9 para senales criticas porque intervienen en
el arranque: GPIO8 suele llevar el LED integrado y GPIO9 es el boton de
arranque. Si se agrega un display I2C, usar dos pines libres para SDA y SCL
y verificar que no interfieran con el arranque.

## Advertencias por la logica de 3.3V

La ESP32-C3 trabaja a 3.3V. Meter 5V a un pin puede danarla.

- **Sensores IR:** alimentarlos a 3.3V, para que su salida digital sea de
  3.3V.
- **MQ-2:** su VCC va a 5V, porque el calentador lo necesita, pero la salida
  analogica debe pasar por un divisor de voltaje que la baje a un maximo de
  3.3V antes de entrar al pin. Conectarla a un pin del ADC1 (GPIO0 a GPIO4):
  el ADC2 no funciona con el WiFi encendido.
- **Servos SG90:** alimentarlos desde la fuente externa de 5V con al menos
  2A, con tierra comun con la placa. Dos SG90 alzando la barrera pueden
  pedir picos combinados de mas de 1A, y alimentarlos flojo reinicia la
  ESP32 por ruido en la tierra comun; conviene un condensador de reserva (de
  470 a 1000 uF) cerca de los servos. La senal de 3.3V de la placa les
  sirve.
- **Buzzer activo:** se controla con una salida digital.

## Esquema de conexion

```
                      +-------------------+
   Fuente 5V 2A ----->| VCC servos        |
        |             |                   |
        +-- GND ------+---- GND comun ----+---- GND ESP32-C3
                                          |
   ESP32-C3 3.3V ---------> VCC sensores IR
   ESP32-C3 GPIO5 <-------- OUT IR entrada
   ESP32-C3 GPIO6 <-------- OUT IR salida
   ESP32-C3 GPIO7 --------> Senal servo entrada
   ESP32-C3 GPIO10 -------> Senal servo salida
   ESP32-C3 GPIO4 --------> Senal buzzer

   MQ-2 VCC --- 5V
   MQ-2 AOUT --[ R1 ]--+--[ R2 ]-- GND        divisor de voltaje
                       |
                       +--> ESP32-C3 GPIO0    maximo 3.3V
```

Con R1 de 10 kilohmios y R2 de 20 kilohmios, una salida de 5V del MQ-2 llega
al pin como 3.3V.

## Notas de puesta en marcha

- El MQ-2 necesita calentar: durante los primeros 60 a 120 segundos sus
  lecturas son altas y falsas. El firmware las ignora en esa ventana, pero
  el control de acceso arranca de inmediato.
- Antes de cargar el firmware hay que ajustar en `firmware/smart_parking.ino`
  el SSID, la contrasena del WiFi, la IP del backend y el token del
  dispositivo.
- Placa en el IDE de Arduino: ESP32C3 Dev Module, con USB CDC habilitado.
