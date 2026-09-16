package com.unillanos.smartparking.infraestructura.dispositivo;

import com.unillanos.smartparking.aplicacion.EjecutorSerie;
import com.unillanos.smartparking.dominio.evento.DispositivoConexionCambiadaEvento;
import com.unillanos.smartparking.dominio.modelo.EventoSistema;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.entrada.ConsultarEstadoCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.ControlManualCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.GestionarConfiguracionCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.ProcesarLecturaHumoCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.RegistrarEntradaCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.RegistrarSalidaCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/** Adaptador de entrada del lado del hardware: decodifica y despacha. */
@Component
public class ManejadorDispositivoWebSocket extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorDispositivoWebSocket.class);

    private final SesionDispositivo sesion;
    private final CodificadorMensajesDispositivo codificador;
    private final PropiedadesDispositivo propiedades;
    private final EjecutorSerie ejecutor;
    private final RegistrarEntradaCasoUso entrada;
    private final RegistrarSalidaCasoUso salida;
    private final ProcesarLecturaHumoCasoUso humo;
    private final ControlManualCasoUso controlManual;
    private final ConsultarEstadoCasoUso consultaEstado;
    private final GestionarConfiguracionCasoUso configuracion;
    private final PuertoPasarelaDispositivo pasarela;
    private final PuertoPublicadorEventos publicador;
    private final RepositorioEventoSistema repositorioEventos;

    public ManejadorDispositivoWebSocket(SesionDispositivo sesion,
                                         CodificadorMensajesDispositivo codificador,
                                         PropiedadesDispositivo propiedades,
                                         EjecutorSerie ejecutor,
                                         RegistrarEntradaCasoUso entrada,
                                         RegistrarSalidaCasoUso salida,
                                         ProcesarLecturaHumoCasoUso humo,
                                         ControlManualCasoUso controlManual,
                                         ConsultarEstadoCasoUso consultaEstado,
                                         GestionarConfiguracionCasoUso configuracion,
                                         PuertoPasarelaDispositivo pasarela,
                                         PuertoPublicadorEventos publicador,
                                         RepositorioEventoSistema repositorioEventos) {
        this.sesion = sesion;
        this.codificador = codificador;
        this.propiedades = propiedades;
        this.ejecutor = ejecutor;
        this.entrada = entrada;
        this.salida = salida;
        this.humo = humo;
        this.controlManual = controlManual;
        this.consultaEstado = consultaEstado;
        this.configuracion = configuracion;
        this.pasarela = pasarela;
        this.publicador = publicador;
        this.repositorioEventos = repositorioEventos;
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
        publicador.publicar(DispositivoConexionCambiadaEvento.ahora(true));
    }

    @Override
    protected void handleTextMessage(WebSocketSession origen, TextMessage mensaje) {
        MensajeDispositivo entrante;
        try {
            entrante = codificador.decodificar(mensaje.getPayload());
        } catch (CodificadorMensajesDispositivo.MensajeInvalidoException e) {
            LOG.warn("{}", e.getMessage());
            return;
        }
        sesion.registrarLatido();
        despachar(entrante);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession cerrada, CloseStatus estado) {
        LOG.info("Dispositivo desconectado (codigo {})", estado.getCode());
        sesion.limpiar();
        ejecutor.ejecutar(() -> repositorioEventos.guardar(
                EventoSistema.de("DISPOSITIVO_DESCONECTADO", "Se perdio el enlace con la ESP32")));
        publicador.publicar(DispositivoConexionCambiadaEvento.ahora(false));
    }

    private void despachar(MensajeDispositivo mensaje) {
        switch (mensaje.tipo()) {
            case MensajeDispositivo.LISTO -> ejecutor.ejecutar(this::sincronizarDispositivo);
            case MensajeDispositivo.ENTRADA_DETECTADA -> ejecutor.ejecutar(entrada::alDetectarEntrada);
            case MensajeDispositivo.SALIDA_DETECTADA -> ejecutor.ejecutar(salida::alDetectarSalida);
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
            ejecutor.ejecutar(entrada::confirmarEntrada);
        } else {
            ejecutor.ejecutar(salida::confirmarSalida);
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
        pasarela.configurarUmbral(configuracion.obtenerConfiguracion().umbralHumo());
        pasarela.actualizarPantalla(consultaEstado.obtenerEstado().cuposDisponibles());
        repositorioEventos.guardar(EventoSistema.de("DISPOSITIVO_CONECTADO", "La ESP32 reporto LISTO"));
    }

    private boolean tokenValido(WebSocketSession nueva) {
        String consulta = nueva.getUri() == null ? null : nueva.getUri().getQuery();
        if (consulta == null) {
            return false;
        }
        for (String parte : consulta.split("&")) {
            String[] par = parte.split("=", 2);
            if (par.length == 2 && "token".equals(par[0])) {
                return propiedades.getToken().equals(par[1]);
            }
        }
        return false;
    }
}
