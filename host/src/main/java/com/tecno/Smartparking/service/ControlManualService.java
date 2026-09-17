package com.tecno.Smartparking.service;

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

/** Apertura y cierre de barreras por orden del operador desde el tablero. */
@Service
public class ControlManualService {

    private static final Logger LOG = LoggerFactory.getLogger(ControlManualService.class);

    private final Barrera barreraEntrada;
    private final Barrera barreraSalida;
    private final EventoSistemaRepository eventos;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final NotificacionService notificaciones;

    public ControlManualService(@Qualifier("barreraEntrada") Barrera barreraEntrada,
                                @Qualifier("barreraSalida") Barrera barreraSalida,
                                EventoSistemaRepository eventos,
                                FabricaComandos comandos,
                                InvocadorComandos invocador,
                                NotificacionService notificaciones) {
        this.barreraEntrada = barreraEntrada;
        this.barreraSalida = barreraSalida;
        this.eventos = eventos;
        this.comandos = comandos;
        this.invocador = invocador;
        this.notificaciones = notificaciones;
    }

    @Transactional
    public void abrirBarrera(TipoPunto punto) {
        Barrera barrera = barreraDe(punto);
        barrera.autorizar();
        invocador.ejecutar(comandos.abrirBarrera(punto));
        barrera.marcarAbierta();
        eventos.save(EventoSistema.de("CONTROL_MANUAL", "Apertura manual de " + punto));
    }

    @Transactional
    public void cerrarBarrera(TipoPunto punto) {
        Barrera barrera = barreraDe(punto);
        barrera.marcarVehiculoPaso();
        invocador.ejecutar(comandos.cerrarBarrera(punto));
        barrera.marcarCerrada();
        eventos.save(EventoSistema.de("CONTROL_MANUAL", "Cierre manual de " + punto));
    }

    /** Condicion segura: la barrera se queda arriba y se avisa al operador. */
    @Transactional
    public void alBloquearseBarrera(TipoPunto punto) {
        LOG.warn("Barrera de {} bloqueada: hay un vehiculo detenido en el punto", punto);
        Barrera barrera = barreraDe(punto);
        barrera.marcarVehiculoPresente();
        barrera.marcarAbierta();

        eventos.save(EventoSistema.de("BARRERA_BLOQUEADA",
                "Vehiculo detenido en " + punto + "; la barrera permanece abierta"));
        notificaciones.barreraBloqueada(punto);
    }

    private Barrera barreraDe(TipoPunto punto) {
        return punto == TipoPunto.ENTRADA ? barreraEntrada : barreraSalida;
    }
}
