package com.unillanos.smartparking.aplicacion.servicio;

import com.unillanos.smartparking.aplicacion.EjecutorSerie;
import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.barrera.Barrera;
import com.unillanos.smartparking.dominio.evento.CuposCambiadosEvento;
import com.unillanos.smartparking.dominio.evento.VehiculoEgresadoEvento;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import com.unillanos.smartparking.dominio.puerto.entrada.RegistrarSalidaCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Salida de un vehiculo. La barrera de salida abre siempre, sin condicionar
 * a cupos; el cupo se devuelve al confirmarse el paso.
 */
public class RegistrarSalidaServicio implements RegistrarSalidaCasoUso {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrarSalidaServicio.class);

    private final EstadoParqueadero estadoParqueadero;
    private final Barrera barreraSalida;
    private final RepositorioRegistroAcceso repositorioRegistros;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final PuertoPublicadorEventos publicador;
    private final EjecutorSerie ejecutor;
    private final long esperaPasoMs;

    private boolean salidaEnCurso;
    private ScheduledFuture<?> vencimientoEspera;

    public RegistrarSalidaServicio(EstadoParqueadero estadoParqueadero,
                                   Barrera barreraSalida,
                                   RepositorioRegistroAcceso repositorioRegistros,
                                   FabricaComandos comandos,
                                   InvocadorComandos invocador,
                                   PuertoPublicadorEventos publicador,
                                   EjecutorSerie ejecutor,
                                   long esperaPasoMs) {
        this.estadoParqueadero = estadoParqueadero;
        this.barreraSalida = barreraSalida;
        this.repositorioRegistros = repositorioRegistros;
        this.comandos = comandos;
        this.invocador = invocador;
        this.publicador = publicador;
        this.ejecutor = ejecutor;
        this.esperaPasoMs = esperaPasoMs;
    }

    @Override
    public void alDetectarSalida() {
        if (salidaEnCurso) {
            return;
        }
        salidaEnCurso = true;

        barreraSalida.autorizar();
        invocador.ejecutar(comandos.abrirBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarAbierta();

        vencimientoEspera = ejecutor.programar(this::alVencerEspera, esperaPasoMs);
    }

    @Override
    public void confirmarSalida() {
        cancelarVencimiento();
        if (!salidaEnCurso) {
            return;
        }
        salidaEnCurso = false;

        estadoParqueadero.registrarSalida();
        cerrarVisitaMasAntigua();

        invocador.ejecutar(comandos.actualizarPantalla(estadoParqueadero.getCuposDisponibles()));
        publicador.publicar(CuposCambiadosEvento.ahora(
                estadoParqueadero.getCuposDisponibles(), estadoParqueadero.getCapacidadTotal()));

        barreraSalida.marcarVehiculoPaso();
        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarCerrada();
    }

    /** Sin lectura de placas, la salida se empareja con el ingreso activo mas antiguo. */
    private void cerrarVisitaMasAntigua() {
        Optional<RegistroAcceso> activo = repositorioRegistros.buscarActivoMasAntiguo();
        if (activo.isEmpty()) {
            LOG.warn("Salida sin registro activo que cerrar");
            return;
        }
        RegistroAcceso registro = activo.get();
        registro.finalizar(LocalDateTime.now());
        repositorioRegistros.guardar(registro);
        publicador.publicar(VehiculoEgresadoEvento.ahora(registro.getId()));
    }

    private void alVencerEspera() {
        if (!salidaEnCurso) {
            return;
        }
        LOG.info("Vencio la espera del paso en la salida; se cierra sin contar la salida");
        salidaEnCurso = false;

        barreraSalida.marcarTiempoDeEspera();
        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarCerrada();
    }

    private void cancelarVencimiento() {
        if (vencimientoEspera != null) {
            vencimientoEspera.cancel(false);
            vencimientoEspera = null;
        }
    }
}
