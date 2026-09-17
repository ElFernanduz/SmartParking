package com.tecno.Smartparking.service;

import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.model.EventoSistema;
import com.tecno.Smartparking.model.RegistroAcceso;
import com.tecno.Smartparking.model.TipoPunto;
import com.tecno.Smartparking.repository.EventoSistemaRepository;
import com.tecno.Smartparking.repository.RegistroAccesoRepository;
import com.tecno.Smartparking.service.barrera.Barrera;
import com.tecno.Smartparking.service.comando.FabricaComandos;
import com.tecno.Smartparking.service.comando.InvocadorComandos;
import com.tecno.Smartparking.service.politica.PoliticaAcceso;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ingreso y salida de vehiculos. El cupo se reserva al autorizar el ingreso
 * y se confirma cuando el vehiculo termina de pasar; si vence la espera sin
 * paso, la reserva se revierte.
 */
@Service
public class AccesoService {

    private static final Logger LOG = LoggerFactory.getLogger(AccesoService.class);

    private final EstadoParqueaderoService estado;
    private final PoliticaAcceso politica;
    private final Barrera barreraEntrada;
    private final Barrera barreraSalida;
    private final RegistroAccesoRepository registros;
    private final EventoSistemaRepository eventos;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final NotificacionService notificaciones;
    private final EjecutorEventos ejecutor;
    private final long esperaPasoMs;

    private RegistroAcceso reserva;
    private ScheduledFuture<?> vencimientoEntrada;
    private boolean salidaEnCurso;
    private ScheduledFuture<?> vencimientoSalida;

    public AccesoService(EstadoParqueaderoService estado,
                         PoliticaAcceso politica,
                         @Qualifier("barreraEntrada") Barrera barreraEntrada,
                         @Qualifier("barreraSalida") Barrera barreraSalida,
                         RegistroAccesoRepository registros,
                         EventoSistemaRepository eventos,
                         FabricaComandos comandos,
                         InvocadorComandos invocador,
                         NotificacionService notificaciones,
                         EjecutorEventos ejecutor,
                         @Value("${smartparking.dispositivo.espera-paso-ms:10000}") long esperaPasoMs) {
        this.estado = estado;
        this.politica = politica;
        this.barreraEntrada = barreraEntrada;
        this.barreraSalida = barreraSalida;
        this.registros = registros;
        this.eventos = eventos;
        this.comandos = comandos;
        this.invocador = invocador;
        this.notificaciones = notificaciones;
        this.ejecutor = ejecutor;
        this.esperaPasoMs = esperaPasoMs;
    }

    @Transactional
    public void alDetectarEntrada() {
        if (reserva != null) {
            return;
        }
        if (!politica.admiteIngreso(estado)) {
            LOG.info("Ingreso no autorizado: {} cupos, estado {}",
                    estado.getCuposDisponibles(), estado.getEstado());
            eventos.save(EventoSistema.de("INGRESO_RECHAZADO", "Sin cupo o sistema no operativo"));
            return;
        }

        estado.registrarEntrada();
        reserva = registros.save(RegistroAcceso.nuevaVisita(LocalDateTime.now()));

        barreraEntrada.autorizar();
        invocador.ejecutar(comandos.abrirBarrera(TipoPunto.ENTRADA));
        barreraEntrada.marcarAbierta();

        invocador.ejecutar(comandos.actualizarPantalla(estado.getCuposDisponibles()));
        notificaciones.vehiculoIngresado(reserva.getId());
        notificarCupos();

        vencimientoEntrada = ejecutor.programar(this::alVencerEsperaEntrada, esperaPasoMs);
    }

    @Transactional
    public void confirmarEntrada() {
        cancelar(vencimientoEntrada);
        vencimientoEntrada = null;
        if (reserva == null) {
            return;
        }
        reserva = null;

        barreraEntrada.marcarVehiculoPaso();
        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.ENTRADA));
        barreraEntrada.marcarCerrada();
    }

    @Transactional
    public void alDetectarSalida() {
        if (salidaEnCurso) {
            return;
        }
        salidaEnCurso = true;

        barreraSalida.autorizar();
        invocador.ejecutar(comandos.abrirBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarAbierta();

        vencimientoSalida = ejecutor.programar(this::alVencerEsperaSalida, esperaPasoMs);
    }

    @Transactional
    public void confirmarSalida() {
        cancelar(vencimientoSalida);
        vencimientoSalida = null;
        if (!salidaEnCurso) {
            return;
        }
        salidaEnCurso = false;

        estado.registrarSalida();
        cerrarVisitaMasAntigua();

        invocador.ejecutar(comandos.actualizarPantalla(estado.getCuposDisponibles()));
        notificarCupos();

        barreraSalida.marcarVehiculoPaso();
        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarCerrada();
    }

    /** Sin lectura de placas, la salida se empareja con el ingreso activo mas antiguo. */
    private void cerrarVisitaMasAntigua() {
        Optional<RegistroAcceso> activo =
                registros.findFirstByEstadoVisitaOrderByHoraEntradaAsc(EstadoVisita.ACTIVO);
        if (activo.isEmpty()) {
            LOG.warn("Salida sin registro activo que cerrar");
            return;
        }
        RegistroAcceso registro = activo.get();
        registro.finalizar(LocalDateTime.now());
        registros.save(registro);
        notificaciones.vehiculoEgresado(registro.getId());
    }

    /** Vencio la espera sin que el vehiculo cruzara: se cierra y se devuelve el cupo. */
    @Transactional
    public void alVencerEsperaEntrada() {
        if (reserva == null) {
            return;
        }
        LOG.info("Vencio la espera del paso en la entrada; se revierte la reserva");

        barreraEntrada.marcarTiempoDeEspera();
        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.ENTRADA));
        barreraEntrada.marcarCerrada();

        registros.delete(reserva);
        reserva = null;
        estado.registrarSalida();

        invocador.ejecutar(comandos.actualizarPantalla(estado.getCuposDisponibles()));
        eventos.save(EventoSistema.de("RESERVA_REVERTIDA",
                "El vehiculo no completo el paso por la entrada"));
        notificarCupos();
    }

    @Transactional
    public void alVencerEsperaSalida() {
        if (!salidaEnCurso) {
            return;
        }
        LOG.info("Vencio la espera del paso en la salida; se cierra sin contar la salida");
        salidaEnCurso = false;

        barreraSalida.marcarTiempoDeEspera();
        invocador.ejecutar(comandos.cerrarBarrera(TipoPunto.SALIDA));
        barreraSalida.marcarCerrada();
    }

    private void cancelar(ScheduledFuture<?> vencimiento) {
        if (vencimiento != null) {
            vencimiento.cancel(false);
        }
    }

    private void notificarCupos() {
        notificaciones.cuposCambiados(estado.getCuposDisponibles(), estado.getCapacidadTotal());
    }
}
