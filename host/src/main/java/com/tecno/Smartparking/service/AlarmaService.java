package com.tecno.Smartparking.service;

import com.tecno.Smartparking.service.comando.FabricaComandos;
import com.tecno.Smartparking.service.comando.InvocadorComandos;
import org.springframework.stereotype.Service;

/** Estado de la alarma sonora, consultado por el tablero. */
@Service
public class AlarmaService {

    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final NotificacionService notificaciones;

    private boolean activa;

    public AlarmaService(FabricaComandos comandos, InvocadorComandos invocador,
                         NotificacionService notificaciones) {
        this.comandos = comandos;
        this.invocador = invocador;
        this.notificaciones = notificaciones;
    }

    public synchronized void activar() {
        if (activa) {
            return;
        }
        invocador.ejecutar(comandos.activarAlarma());
        activa = true;
        notificaciones.alarmaCambiada(true);
    }

    public synchronized void silenciar() {
        if (!activa) {
            return;
        }
        invocador.ejecutar(comandos.silenciarAlarma());
        activa = false;
        notificaciones.alarmaCambiada(false);
    }

    public synchronized boolean estaActiva() {
        return activa;
    }
}
