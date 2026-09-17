package com.tecno.Smartparking.websocket;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import com.tecno.Smartparking.model.TipoPunto;
import org.springframework.stereotype.Component;

/** Traduce entre los mensajes del protocolo y su representacion JSON. */
@Component
public class CodificadorMensajes {

    private final JsonMapper mapper = JsonMapper.builder().build();

    public MensajeDispositivo decodificar(String json) {
        try {
            JsonNode nodo = mapper.readTree(json);
            String tipo = nodo.path("tipo").asText(null);
            if (tipo == null || tipo.isBlank()) {
                throw new MensajeInvalidoException("El mensaje no trae tipo");
            }
            TipoPunto punto = nodo.hasNonNull("punto")
                    ? TipoPunto.valueOf(nodo.get("punto").asText())
                    : null;
            Integer nivel = nodo.hasNonNull("nivel") ? nodo.get("nivel").asInt() : null;
            return new MensajeDispositivo(tipo, punto, nivel);
        } catch (MensajeInvalidoException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new MensajeInvalidoException("Mensaje ilegible del dispositivo: " + e.getMessage());
        }
    }

    public String abrir(TipoPunto punto) {
        ObjectNode nodo = mapper.createObjectNode();
        nodo.put("tipo", "ABRIR");
        nodo.put("punto", punto.name());
        return nodo.toString();
    }

    public String cerrar(TipoPunto punto) {
        ObjectNode nodo = mapper.createObjectNode();
        nodo.put("tipo", "CERRAR");
        nodo.put("punto", punto.name());
        return nodo.toString();
    }

    public String alarma(boolean encendida) {
        ObjectNode nodo = mapper.createObjectNode();
        nodo.put("tipo", "ALARMA");
        nodo.put("estado", encendida ? "ON" : "OFF");
        return nodo.toString();
    }

    public String pantalla(int cupos) {
        ObjectNode nodo = mapper.createObjectNode();
        nodo.put("tipo", "PANTALLA");
        nodo.put("cupos", cupos);
        return nodo.toString();
    }

    public String configurarUmbral(int umbral) {
        ObjectNode nodo = mapper.createObjectNode();
        nodo.put("tipo", "CONFIG_UMBRAL");
        nodo.put("umbral", umbral);
        return nodo.toString();
    }

    public String ping() {
        ObjectNode nodo = mapper.createObjectNode();
        nodo.put("tipo", "PING");
        return nodo.toString();
    }

    public static class MensajeInvalidoException extends RuntimeException {

        public MensajeInvalidoException(String mensaje) {
            super(mensaje);
        }
    }
}
