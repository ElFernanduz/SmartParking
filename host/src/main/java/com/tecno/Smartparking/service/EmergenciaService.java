package com.tecno.Smartparking.service;

import com.tecno.Smartparking.model.EstadoOperativo;
import com.tecno.Smartparking.model.EventoSistema;
import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.repository.EventoSistemaRepository;
import com.tecno.Smartparking.service.barrera.Barrera;
import com.tecno.Smartparking.service.comando.FabricaComandos;
import com.tecno.Smartparking.service.comando.InvocadorComandos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Protocolo de emergencia: habilita la salida, bloquea los ingresos y
 * sostiene la alarma hasta que un operador la limpie.
 */
@Service
public class EmergenciaService {

    private static final Logger LOG = LoggerFactory.getLogger(EmergenciaService.class);

    private final EstadoParqueaderoService estado;
    private final AlarmaService alarma;
    private final Barrera barreraSalida;
    private final ConfiguracionService configuracion;
    private final EventoSistemaRepository eventos;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final NotificacionService notificaciones;

    public EmergenciaService(EstadoParqueaderoService estado,
                             AlarmaService alarma,
                             @Qualifier("barreraSalida") Barrera barreraSalida,
                             ConfiguracionService configuracion,
                             EventoSistemaRepository eventos,
                             FabricaComandos comandos,
                             InvocadorComandos invocador,
                             NotificacionService notificaciones) {
        this.estado = estado;
        this.alarma = alarma;
        this.barreraSalida = barreraSalida;
        this.configuracion = configuracion;
        this.eventos = eventos;
        this.comandos = comandos;
        this.invocador = invocador;
        this.notificaciones = notificaciones;
    }

    @Transactional
    public void activar() {
        if (estado.getEstado() == EstadoOperativo.EMERGENCIA) {
            return;
        }
        LOG.warn("Protocolo de emergencia activado");

        estado.cambiarEstado(EstadoOperativo.EMERGENCIA);
        configuracion.guardarEstadoSistema(EstadoOperativo.EMERGENCIA);

        barreraSalida.autorizar();
        invocador.ejecutar(comandos.abrirBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarAbierta();

        alarma.activar();

        eventos.save(EventoSistema.de("EMERGENCIA_ACTIVADA", "Salida habilitada e ingresos bloqueados"));
        notificaciones.estadoSistemaCambiado(estado.getEstado());
    }

    @Transactional
    public void limpiar() {
        if (estado.getEstado() != EstadoOperativo.EMERGENCIA) {
            return;
        }
        LOG.info("Emergencia atendida; el sistema vuelve a operar");

        alarma.silenciar();

        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarVehiculoPaso();
        barreraSalida.marcarCerrada();

        estado.cambiarEstado(EstadoOperativo.OPERATIVO);
        configuracion.guardarEstadoSistema(estado.getEstado());

        eventos.save(EventoSistema.de("EMERGENCIA_LIMPIADA", "Operacion restablecida"));
        notificaciones.estadoSistemaCambiado(estado.getEstado());
    }
}
