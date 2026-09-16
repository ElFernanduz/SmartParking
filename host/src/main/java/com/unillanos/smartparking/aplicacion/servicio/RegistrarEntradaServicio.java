package com.unillanos.smartparking.aplicacion.servicio;

import com.unillanos.smartparking.aplicacion.EjecutorSerie;
import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.barrera.Barrera;
import com.unillanos.smartparking.dominio.evento.CuposCambiadosEvento;
import com.unillanos.smartparking.dominio.evento.VehiculoIngresadoEvento;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.modelo.EventoSistema;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.politica.PoliticaAcceso;
import com.unillanos.smartparking.dominio.puerto.entrada.RegistrarEntradaCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ingreso de un vehiculo. El cupo se reserva al autorizar y se confirma
 * cuando el vehiculo termina de pasar; si vence la espera sin paso, la
 * reserva se revierte.
 */
public class RegistrarEntradaServicio implements RegistrarEntradaCasoUso {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrarEntradaServicio.class);

    private final EstadoParqueadero estadoParqueadero;
    private final PoliticaAcceso politica;
    private final Barrera barreraEntrada;
    private final RepositorioRegistroAcceso repositorioRegistros;
    private final RepositorioEventoSistema repositorioEventos;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final PuertoPublicadorEventos publicador;
    private final EjecutorSerie ejecutor;
    private final long esperaPasoMs;

    private RegistroAcceso reserva;
    private ScheduledFuture<?> vencimientoEspera;

    public RegistrarEntradaServicio(EstadoParqueadero estadoParqueadero,
                                    PoliticaAcceso politica,
                                    Barrera barreraEntrada,
                                    RepositorioRegistroAcceso repositorioRegistros,
                                    RepositorioEventoSistema repositorioEventos,
                                    FabricaComandos comandos,
                                    InvocadorComandos invocador,
                                    PuertoPublicadorEventos publicador,
                                    EjecutorSerie ejecutor,
                                    long esperaPasoMs) {
        this.estadoParqueadero = estadoParqueadero;
        this.politica = politica;
        this.barreraEntrada = barreraEntrada;
        this.repositorioRegistros = repositorioRegistros;
        this.repositorioEventos = repositorioEventos;
        this.comandos = comandos;
        this.invocador = invocador;
        this.publicador = publicador;
        this.ejecutor = ejecutor;
        this.esperaPasoMs = esperaPasoMs;
    }

    @Override
    public void alDetectarEntrada() {
        if (reserva != null) {
            return;
        }
        if (!estadoParqueadero.puedeAdmitir(politica)) {
            LOG.info("Ingreso no autorizado: {} cupos, estado {}",
                    estadoParqueadero.getCuposDisponibles(), estadoParqueadero.getEstado());
            repositorioEventos.guardar(EventoSistema.de("INGRESO_RECHAZADO",
                    "Sin cupo o sistema no operativo"));
            return;
        }

        estadoParqueadero.registrarEntrada();
        reserva = repositorioRegistros.guardar(RegistroAcceso.nuevaVisita(LocalDateTime.now()));

        barreraEntrada.autorizar();
        invocador.ejecutar(comandos.abrirBarrera(TipoPunto.ENTRADA));
        barreraEntrada.marcarAbierta();

        invocador.ejecutar(comandos.actualizarPantalla(estadoParqueadero.getCuposDisponibles()));
        publicador.publicar(VehiculoIngresadoEvento.ahora(reserva.getId()));
        publicarCupos();

        programarVencimiento();
    }

    @Override
    public void confirmarEntrada() {
        cancelarVencimiento();
        if (reserva == null) {
            return;
        }
        reserva = null;

        barreraEntrada.marcarVehiculoPaso();
        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.ENTRADA));
        barreraEntrada.marcarCerrada();
    }

    /** Vencio la espera sin que el vehiculo cruzara: se cierra y se devuelve el cupo. */
    private void alVencerEspera() {
        if (reserva == null) {
            return;
        }
        LOG.info("Vencio la espera del paso en la entrada; se revierte la reserva");

        barreraEntrada.marcarTiempoDeEspera();
        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.ENTRADA));
        barreraEntrada.marcarCerrada();

        repositorioRegistros.eliminar(reserva);
        reserva = null;
        estadoParqueadero.registrarSalida();

        invocador.ejecutar(comandos.actualizarPantalla(estadoParqueadero.getCuposDisponibles()));
        repositorioEventos.guardar(EventoSistema.de("RESERVA_REVERTIDA",
                "El vehiculo no completo el paso por la entrada"));
        publicarCupos();
    }

    private void programarVencimiento() {
        vencimientoEspera = ejecutor.programar(this::alVencerEspera, esperaPasoMs);
    }

    private void cancelarVencimiento() {
        if (vencimientoEspera != null) {
            vencimientoEspera.cancel(false);
            vencimientoEspera = null;
        }
    }

    private void publicarCupos() {
        publicador.publicar(CuposCambiadosEvento.ahora(
                estadoParqueadero.getCuposDisponibles(), estadoParqueadero.getCapacidadTotal()));
    }
}
