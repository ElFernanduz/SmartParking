package com.unillanos.smartparking.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unillanos.smartparking.dominio.evento.EventoDominio;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Observer: cada evento de dominio sale hacia los navegadores conectados. */
@Component
public class PublicadorEventosWebSocket implements PuertoPublicadorEventos {

    private static final Logger LOG = LoggerFactory.getLogger(PublicadorEventosWebSocket.class);

    private final ManejadorTableroWebSocket tablero;
    private final ObjectMapper mapper;

    public PublicadorEventosWebSocket(ManejadorTableroWebSocket tablero) {
        this.tablero = tablero;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void publicar(EventoDominio evento) {
        try {
            ObjectNode sobre = mapper.createObjectNode();
            sobre.put("tipo", evento.getClass().getSimpleName());
            sobre.set("datos", mapper.valueToTree(evento));
            tablero.difundir(sobre.toString());
        } catch (RuntimeException e) {
            LOG.warn("No se pudo publicar el evento {}: {}",
                    evento.getClass().getSimpleName(), e.getMessage());
        }
    }
}
