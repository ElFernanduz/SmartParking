package com.unillanos.smartparking.aplicacion.servicio;

import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.barrera.Barrera;
import com.unillanos.smartparking.dominio.evento.BarreraBloqueadaEvento;
import com.unillanos.smartparking.dominio.modelo.EventoSistema;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.entrada.ControlManualCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Apertura y cierre de barreras por orden del operador desde el tablero. */
public class ControlManualServicio implements ControlManualCasoUso {

    private static final Logger LOG = LoggerFactory.getLogger(ControlManualServicio.class);

    private final Barrera barreraEntrada;
    private final Barrera barreraSalida;
    private final RepositorioEventoSistema repositorioEventos;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final PuertoPublicadorEventos publicador;

    public ControlManualServicio(Barrera barreraEntrada,
                                 Barrera barreraSalida,
                                 RepositorioEventoSistema repositorioEventos,
                                 FabricaComandos comandos,
                                 InvocadorComandos invocador,
                                 PuertoPublicadorEventos publicador) {
        this.barreraEntrada = barreraEntrada;
        this.barreraSalida = barreraSalida;
        this.repositorioEventos = repositorioEventos;
        this.comandos = comandos;
        this.invocador = invocador;
        this.publicador = publicador;
    }

    @Override
    public void abrirBarrera(TipoPunto punto) {
        Barrera barrera = barreraDe(punto);
        barrera.autorizar();
        invocador.ejecutar(comandos.abrirBarrera(punto));
        barrera.marcarAbierta();
        repositorioEventos.guardar(EventoSistema.de("CONTROL_MANUAL", "Apertura manual de " + punto));
    }

    @Override
    public void cerrarBarrera(TipoPunto punto) {
        Barrera barrera = barreraDe(punto);
        barrera.marcarVehiculoPaso();
        invocador.ejecutar(comandos.cerrarBarrera(punto));
        barrera.marcarCerrada();
        repositorioEventos.guardar(EventoSistema.de("CONTROL_MANUAL", "Cierre manual de " + punto));
    }

    /** Condicion segura: la barrera se queda arriba y se avisa al operador. */
    @Override
    public void alBloquearseBarrera(TipoPunto punto) {
        LOG.warn("Barrera de {} bloqueada: hay un vehiculo detenido en el punto", punto);
        Barrera barrera = barreraDe(punto);
        barrera.marcarVehiculoPresente();
        barrera.marcarAbierta();

        repositorioEventos.guardar(EventoSistema.de("BARRERA_BLOQUEADA",
                "Vehiculo detenido en " + punto + "; la barrera permanece abierta"));
        publicador.publicar(BarreraBloqueadaEvento.ahora(punto));
    }

    private Barrera barreraDe(TipoPunto punto) {
        return punto == TipoPunto.ENTRADA ? barreraEntrada : barreraSalida;
    }
}
