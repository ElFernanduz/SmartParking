package com.tecno.Smartparking.websocket;

import com.tecno.Smartparking.model.EventoSistema;
import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.repository.EventoSistemaRepository;
import com.tecno.Smartparking.service.AccesoService;
import com.tecno.Smartparking.service.ConfiguracionService;
import com.tecno.Smartparking.service.ControlManualService;
import com.tecno.Smartparking.service.DispositivoService;
import com.tecno.Smartparking.service.EjecutorEventos;
import com.tecno.Smartparking.service.EstadoParqueaderoService;
import com.tecno.Smartparking.service.HumoService;
import com.tecno.Smartparking.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/** Recibe los eventos de la ESP32, los decodifica y los despacha a los servicios. */
@Component
public class DispositivoWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DispositivoWebSocketHandler.class);

    private final SesionDispositivo sesion;
    private final CodificadorMensajes codificador;
    private final EjecutorEventos ejecutor;
    private final AccesoService acceso;
    private final HumoService humo;
    private final ControlManualService controlManual;
    private final ConfiguracionService configuracion;
    private final EstadoParqueaderoService estado;
    private final DispositivoService dispositivo;
    private final NotificacionService notificaciones;
    private final EventoSistemaRepository eventos;
    private final String token;

    public DispositivoWebSocketHandler(SesionDispositivo sesion,
                                       CodificadorMensajes codificador,
                                       EjecutorEventos ejecutor,
                                       AccesoService acceso,
                                       HumoService humo,
                                       ControlManualService controlManual,
                                       ConfiguracionService configuracion,
                                       EstadoParqueaderoService estado,
                                       DispositivoService dispositivo,
                                       NotificacionService notificaciones,
                                       EventoSistemaRepository eventos,
                                       @Value("${smartparking.dispositivo.token}") String token) {
        this.sesion = sesion;
        this.codificador = codificador;
        this.ejecutor = ejecutor;
        this.acceso = acceso;
        this.humo = humo;
        this.controlManual = controlManual;
        this.configuracion = configuracion;
        this.estado = estado;
        this.dispositivo = dispositivo;
        this.notificaciones = notificaciones;
        this.eventos = eventos;
        this.token = token;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession nueva) throws Exception {
        if (!tokenValido(nueva)) {
            LOG.warn("Conexion de dispositivo rechazada por token invalido");
            nueva.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        LOG.info("Dispositivo conectado");
        sesion.registrar(nueva);
        notificaciones.dispositivoConexion(true);
    }

    @Override
    protected void handleTextMessage(WebSocketSession origen, TextMessage mensaje) {
        MensajeDispositivo entrante;
        try {
            entrante = codificador.decodificar(mensaje.getPayload());
        } catch (CodificadorMensajes.MensajeInvalidoException e) {
            LOG.warn("{}", e.getMessage());
            return;
        }
        sesion.registrarLatido();
        despachar(entrante);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession cerrada, CloseStatus estadoCierre) {
        LOG.info("Dispositivo desconectado (codigo {})", estadoCierre.getCode());
        sesion.limpiar();
        ejecutor.ejecutar(() -> eventos.save(
                EventoSistema.de("DISPOSITIVO_DESCONECTADO", "Se perdio el enlace con la ESP32")));
        notificaciones.dispositivoConexion(false);
    }

    private void despachar(MensajeDispositivo mensaje) {
        switch (mensaje.tipo()) {
            case MensajeDispositivo.LISTO -> ejecutor.ejecutar(this::sincronizarDispositivo);
            case MensajeDispositivo.ENTRADA_DETECTADA -> ejecutor.ejecutar(acceso::alDetectarEntrada);
            case MensajeDispositivo.SALIDA_DETECTADA -> ejecutor.ejecutar(acceso::alDetectarSalida);
            case MensajeDispositivo.PASO_COMPLETADO -> confirmarPaso(mensaje.punto());
            case MensajeDispositivo.TELEMETRIA_HUMO -> procesarHumo(mensaje.nivel());
            case MensajeDispositivo.BARRERA_BLOQUEADA -> reportarBloqueo(mensaje.punto());
            case MensajeDispositivo.LATIDO -> LOG.debug("Latido recibido");
            default -> LOG.warn("Tipo de mensaje desconocido: {}", mensaje.tipo());
        }
    }

    private void confirmarPaso(TipoPunto punto) {
        if (punto == null) {
            LOG.warn("PASO_COMPLETADO sin punto");
            return;
        }
        if (punto == TipoPunto.ENTRADA) {
            ejecutor.ejecutar(acceso::confirmarEntrada);
        } else {
            ejecutor.ejecutar(acceso::confirmarSalida);
        }
    }

    private void procesarHumo(Integer nivel) {
        if (nivel == null) {
            LOG.warn("TELEMETRIA_HUMO sin nivel");
            return;
        }
        ejecutor.ejecutar(() -> humo.procesarLectura(nivel));
    }

    private void reportarBloqueo(TipoPunto punto) {
        if (punto == null) {
            LOG.warn("BARRERA_BLOQUEADA sin punto");
            return;
        }
        ejecutor.ejecutar(() -> controlManual.alBloquearseBarrera(punto));
    }

    /** Al arrancar el firmware se le propaga el umbral vigente y los cupos. */
    private void sincronizarDispositivo() {
        dispositivo.configurarUmbral(configuracion.obtenerUmbral());
        dispositivo.actualizarPantalla(estado.getCuposDisponibles());
        eventos.save(EventoSistema.de("DISPOSITIVO_CONECTADO", "La ESP32 reporto LISTO"));
    }

    private boolean tokenValido(WebSocketSession nueva) {
        String consulta = nueva.getUri() == null ? null : nueva.getUri().getQuery();
        if (consulta == null) {
            return false;
        }
        for (String parte : consulta.split("&")) {
            String[] par = parte.split("=", 2);
            if (par.length == 2 && "token".equals(par[0])) {
                return token.equals(par[1]);
            }
        }
        return false;
    }
}
