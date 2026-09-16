package com.unillanos.smartparking.aplicacion.servicio;

import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.evento.CuposCambiadosEvento;
import com.unillanos.smartparking.dominio.excepcion.ValorInvalidoExcepcion;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.modelo.EventoSistema;
import com.unillanos.smartparking.dominio.modelo.RegistroAcceso;
import com.unillanos.smartparking.dominio.puerto.entrada.SincronizarCuposCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioEventoSistema;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioRegistroAcceso;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recalibracion manual del conteo. Ajusta el estado en memoria y reconcilia
 * los registros activos para que el valor sobreviva a un reinicio.
 */
public class SincronizarCuposServicio implements SincronizarCuposCasoUso {

    private static final Logger LOG = LoggerFactory.getLogger(SincronizarCuposServicio.class);

    private final EstadoParqueadero estadoParqueadero;
    private final RepositorioRegistroAcceso repositorioRegistros;
    private final RepositorioEventoSistema repositorioEventos;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final PuertoPublicadorEventos publicador;

    public SincronizarCuposServicio(EstadoParqueadero estadoParqueadero,
                                    RepositorioRegistroAcceso repositorioRegistros,
                                    RepositorioEventoSistema repositorioEventos,
                                    FabricaComandos comandos,
                                    InvocadorComandos invocador,
                                    PuertoPublicadorEventos publicador) {
        this.estadoParqueadero = estadoParqueadero;
        this.repositorioRegistros = repositorioRegistros;
        this.repositorioEventos = repositorioEventos;
        this.comandos = comandos;
        this.invocador = invocador;
        this.publicador = publicador;
    }

    @Override
    public void fijarCuposOcupados(int ocupados) {
        int capacidad = estadoParqueadero.getCapacidadTotal();
        if (ocupados < 0 || ocupados > capacidad) {
            throw new ValorInvalidoExcepcion("Los cupos ocupados deben estar entre 0 y " + capacidad);
        }
        LOG.info("Recalibracion del conteo: {} cupos ocupados", ocupados);

        estadoParqueadero.fijarCuposDisponibles(capacidad - ocupados);
        reconciliarRegistrosActivos(ocupados);

        invocador.ejecutar(comandos.actualizarPantalla(estadoParqueadero.getCuposDisponibles()));
        repositorioEventos.guardar(EventoSistema.de("CUPOS_SINCRONIZADOS",
                "Cupos ocupados fijados en " + ocupados));
        publicador.publicar(CuposCambiadosEvento.ahora(
                estadoParqueadero.getCuposDisponibles(), estadoParqueadero.getCapacidadTotal()));
    }

    /** Deja tantas visitas activas como cupos ocupados haya realmente. */
    private void reconciliarRegistrosActivos(int ocupados) {
        int activos = repositorioRegistros.contarActivos();

        while (activos > ocupados) {
            Optional<RegistroAcceso> masAntiguo = repositorioRegistros.buscarActivoMasAntiguo();
            if (masAntiguo.isEmpty()) {
                return;
            }
            RegistroAcceso registro = masAntiguo.get();
            registro.finalizar(LocalDateTime.now());
            repositorioRegistros.guardar(registro);
            activos--;
        }

        while (activos < ocupados) {
            repositorioRegistros.guardar(RegistroAcceso.nuevaVisita(LocalDateTime.now()));
            activos++;
        }
    }
}
