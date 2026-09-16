package com.unillanos.smartparking.aplicacion.servicio;

import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.barrera.Barrera;
import com.unillanos.smartparking.dominio.evento.AlarmaCambiadaEvento;
import com.unillanos.smartparking.dominio.evento.EstadoSistemaCambiadoEvento;
import com.unillanos.smartparking.dominio.modelo.EstadoAlarma;
import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.modelo.EventoSistema;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.entrada.EjecutarEmergenciaCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioConfiguracion;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocolo de emergencia: habilita la salida, bloquea los ingresos y
 * sostiene la alarma hasta que un operador la limpie.
 */
public class EjecutarEmergenciaServicio implements EjecutarEmergenciaCasoUso {

    private static final Logger LOG = LoggerFactory.getLogger(EjecutarEmergenciaServicio.class);

    private final EstadoParqueadero estadoParqueadero;
    private final EstadoAlarma estadoAlarma;
    private final Barrera barreraSalida;
    private final RepositorioConfiguracion repositorioConfiguracion;
    private final RepositorioEventoSistema repositorioEventos;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final PuertoPublicadorEventos publicador;

    public EjecutarEmergenciaServicio(EstadoParqueadero estadoParqueadero,
                                      EstadoAlarma estadoAlarma,
                                      Barrera barreraSalida,
                                      RepositorioConfiguracion repositorioConfiguracion,
                                      RepositorioEventoSistema repositorioEventos,
                                      FabricaComandos comandos,
                                      InvocadorComandos invocador,
                                      PuertoPublicadorEventos publicador) {
        this.estadoParqueadero = estadoParqueadero;
        this.estadoAlarma = estadoAlarma;
        this.barreraSalida = barreraSalida;
        this.repositorioConfiguracion = repositorioConfiguracion;
        this.repositorioEventos = repositorioEventos;
        this.comandos = comandos;
        this.invocador = invocador;
        this.publicador = publicador;
    }

    @Override
    public void activarEmergencia() {
        if (estadoParqueadero.getEstado() == EstadoOperativo.EMERGENCIA) {
            return;
        }
        LOG.warn("Protocolo de emergencia activado");

        estadoParqueadero.cambiarEstado(EstadoOperativo.EMERGENCIA);
        repositorioConfiguracion.guardarEstadoSistema(EstadoOperativo.EMERGENCIA);

        barreraSalida.autorizar();
        invocador.ejecutar(comandos.abrirBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarAbierta();

        if (!estadoAlarma.estaActiva()) {
            invocador.ejecutar(comandos.activarAlarma());
            estadoAlarma.activar();
            publicador.publicar(AlarmaCambiadaEvento.ahora(true));
        }

        repositorioEventos.guardar(EventoSistema.de("EMERGENCIA_ACTIVADA",
                "Salida habilitada e ingresos bloqueados"));
        publicador.publicar(EstadoSistemaCambiadoEvento.ahora(estadoParqueadero.getEstado()));
    }

    @Override
    public void limpiarEmergencia() {
        if (estadoParqueadero.getEstado() != EstadoOperativo.EMERGENCIA) {
            return;
        }
        LOG.info("Emergencia atendida; el sistema vuelve a operar");

        invocador.ejecutar(comandos.silenciarAlarma());
        estadoAlarma.silenciar();
        publicador.publicar(AlarmaCambiadaEvento.ahora(false));

        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarVehiculoPaso();
        barreraSalida.marcarCerrada();

        estadoParqueadero.cambiarEstado(EstadoOperativo.OPERATIVO);
        repositorioConfiguracion.guardarEstadoSistema(estadoParqueadero.getEstado());

        repositorioEventos.guardar(EventoSistema.de("EMERGENCIA_LIMPIADA", "Operacion restablecida"));
        publicador.publicar(EstadoSistemaCambiadoEvento.ahora(estadoParqueadero.getEstado()));
    }
}
