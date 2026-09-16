package com.unillanos.smartparking.aplicacion.servicio;

import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.evento.AlarmaCambiadaEvento;
import com.unillanos.smartparking.dominio.evento.UmbralHumoSuperadoEvento;
import com.unillanos.smartparking.dominio.modelo.EstadoAlarma;
import com.unillanos.smartparking.dominio.modelo.EventoSeguridad;
import com.unillanos.smartparking.dominio.puerto.entrada.EjecutarEmergenciaCasoUso;
import com.unillanos.smartparking.dominio.puerto.entrada.ProcesarLecturaHumoCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioConfiguracion;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSeguridad;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Compara la telemetria de humo contra el umbral configurado. */
public class ProcesarLecturaHumoServicio implements ProcesarLecturaHumoCasoUso {

    private static final Logger LOG = LoggerFactory.getLogger(ProcesarLecturaHumoServicio.class);

    private final RepositorioConfiguracion repositorioConfiguracion;
    private final RepositorioEventoSeguridad repositorioEventos;
    private final EjecutarEmergenciaCasoUso emergencia;
    private final EstadoAlarma estadoAlarma;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final PuertoPublicadorEventos publicador;

    public ProcesarLecturaHumoServicio(RepositorioConfiguracion repositorioConfiguracion,
                                       RepositorioEventoSeguridad repositorioEventos,
                                       EjecutarEmergenciaCasoUso emergencia,
                                       EstadoAlarma estadoAlarma,
                                       FabricaComandos comandos,
                                       InvocadorComandos invocador,
                                       PuertoPublicadorEventos publicador) {
        this.repositorioConfiguracion = repositorioConfiguracion;
        this.repositorioEventos = repositorioEventos;
        this.emergencia = emergencia;
        this.estadoAlarma = estadoAlarma;
        this.comandos = comandos;
        this.invocador = invocador;
        this.publicador = publicador;
    }

    @Override
    public void procesarLectura(int nivel) {
        int umbral = repositorioConfiguracion.obtenerUmbral();
        if (nivel <= umbral) {
            return;
        }
        // Mientras la emergencia siga activa no se repite el registro por cada lectura.
        if (estadoAlarma.estaActiva()) {
            return;
        }
        LOG.warn("Umbral de humo superado: nivel {} sobre umbral {}", nivel, umbral);

        invocador.ejecutar(comandos.activarAlarma());
        estadoAlarma.activar();
        publicador.publicar(AlarmaCambiadaEvento.ahora(true));

        repositorioEventos.guardar(new EventoSeguridad(null, nivel, umbral, LocalDateTime.now(), true));
        publicador.publicar(UmbralHumoSuperadoEvento.ahora(nivel, umbral));

        emergencia.activarEmergencia();
    }
}
