package com.tecno.Smartparking.service;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import com.tecno.Smartparking.model.EstadoOperativo;
import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.websocket.TableroWebSocketHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

/** Avisa al tablero de cada cambio, para que se actualice en vivo. */
@Service
public class NotificacionService {

    public static final String CUPOS_CAMBIADOS = "CUPOS_CAMBIADOS";
    public static final String ESTADO_SISTEMA_CAMBIADO = "ESTADO_SISTEMA_CAMBIADO";
    public static final String ALARMA_CAMBIADA = "ALARMA_CAMBIADA";
    public static final String VEHICULO_INGRESADO = "VEHICULO_INGRESADO";
    public static final String VEHICULO_EGRESADO = "VEHICULO_EGRESADO";
    public static final String UMBRAL_HUMO_SUPERADO = "UMBRAL_HUMO_SUPERADO";
    public static final String BARRERA_BLOQUEADA = "BARRERA_BLOQUEADA";
    public static final String DISPOSITIVO_CONEXION = "DISPOSITIVO_CONEXION";

    private final TableroWebSocketHandler tablero;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public NotificacionService(TableroWebSocketHandler tablero) {
        this.tablero = tablero;
    }

    public void cuposCambiados(int cuposDisponibles, int capacidadTotal) {
        ObjectNode datos = mapper.createObjectNode();
        datos.put("cuposDisponibles", cuposDisponibles);
        datos.put("capacidadTotal", capacidadTotal);
        difundir(CUPOS_CAMBIADOS, datos);
    }

    public void estadoSistemaCambiado(EstadoOperativo estado) {
        ObjectNode datos = mapper.createObjectNode();
        datos.put("estado", estado.name());
        difundir(ESTADO_SISTEMA_CAMBIADO, datos);
    }

    public void alarmaCambiada(boolean activa) {
        ObjectNode datos = mapper.createObjectNode();
        datos.put("activa", activa);
        difundir(ALARMA_CAMBIADA, datos);
    }

    public void vehiculoIngresado(Long registroId) {
        ObjectNode datos = mapper.createObjectNode();
        datos.put("registroId", registroId);
        difundir(VEHICULO_INGRESADO, datos);
    }

    public void vehiculoEgresado(Long registroId) {
        ObjectNode datos = mapper.createObjectNode();
        datos.put("registroId", registroId);
        difundir(VEHICULO_EGRESADO, datos);
    }

    public void umbralHumoSuperado(int nivel, int umbral) {
        ObjectNode datos = mapper.createObjectNode();
        datos.put("nivel", nivel);
        datos.put("umbral", umbral);
        difundir(UMBRAL_HUMO_SUPERADO, datos);
    }

    public void barreraBloqueada(TipoPunto punto) {
        ObjectNode datos = mapper.createObjectNode();
        datos.put("punto", punto.name());
        difundir(BARRERA_BLOQUEADA, datos);
    }

    public void dispositivoConexion(boolean conectado) {
        ObjectNode datos = mapper.createObjectNode();
        datos.put("conectado", conectado);
        difundir(DISPOSITIVO_CONEXION, datos);
    }

    private void difundir(String tipo, ObjectNode datos) {
        ObjectNode sobre = mapper.createObjectNode();
        sobre.put("tipo", tipo);
        sobre.put("ocurridoEn", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        sobre.set("datos", datos);
        tablero.difundir(sobre.toString());
    }
}
