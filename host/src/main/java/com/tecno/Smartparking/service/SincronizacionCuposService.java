package com.tecno.Smartparking.service;

import com.tecno.Smartparking.exception.NegocioException;
import com.tecno.Smartparking.model.EstadoVisita;
import com.tecno.Smartparking.model.EventoSistema;
import com.tecno.Smartparking.model.RegistroAcceso;
import com.tecno.Smartparking.repository.EventoSistemaRepository;
import com.tecno.Smartparking.repository.RegistroAccesoRepository;
import com.tecno.Smartparking.service.comando.FabricaComandos;
import com.tecno.Smartparking.service.comando.InvocadorComandos;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recalibracion manual del conteo. Ajusta el estado en memoria y reconcilia
 * los registros activos para que el valor sobreviva a un reinicio.
 */
@Service
public class SincronizacionCuposService {

    private static final Logger LOG = LoggerFactory.getLogger(SincronizacionCuposService.class);

    private final EstadoParqueaderoService estado;
    private final RegistroAccesoRepository registros;
    private final EventoSistemaRepository eventos;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final NotificacionService notificaciones;

    public SincronizacionCuposService(EstadoParqueaderoService estado,
                                      RegistroAccesoRepository registros,
                                      EventoSistemaRepository eventos,
                                      FabricaComandos comandos,
                                      InvocadorComandos invocador,
                                      NotificacionService notificaciones) {
        this.estado = estado;
        this.registros = registros;
        this.eventos = eventos;
        this.comandos = comandos;
        this.invocador = invocador;
        this.notificaciones = notificaciones;
    }

    @Transactional
    public void fijarCuposOcupados(int ocupados) {
        int capacidad = estado.getCapacidadTotal();
        if (ocupados < 0 || ocupados > capacidad) {
            throw new NegocioException("Los cupos ocupados deben estar entre 0 y " + capacidad);
        }
        LOG.info("Recalibracion del conteo: {} cupos ocupados", ocupados);

        estado.fijarCuposDisponibles(capacidad - ocupados);
        reconciliarRegistrosActivos(ocupados);

        invocador.ejecutar(comandos.actualizarPantalla(estado.getCuposDisponibles()));
        eventos.save(EventoSistema.de("CUPOS_SINCRONIZADOS", "Cupos ocupados fijados en " + ocupados));
        notificaciones.cuposCambiados(estado.getCuposDisponibles(), estado.getCapacidadTotal());
    }

    /** Deja tantas visitas activas como cupos ocupados haya realmente. */
    private void reconciliarRegistrosActivos(int ocupados) {
        int activos = registros.countByEstadoVisita(EstadoVisita.ACTIVO);

        while (activos > ocupados) {
            Optional<RegistroAcceso> masAntiguo =
                    registros.findFirstByEstadoVisitaOrderByHoraEntradaAsc(EstadoVisita.ACTIVO);
            if (masAntiguo.isEmpty()) {
                return;
            }
            RegistroAcceso registro = masAntiguo.get();
            registro.finalizar(LocalDateTime.now());
            registros.save(registro);
            activos--;
        }

        while (activos < ocupados) {
            registros.save(RegistroAcceso.nuevaVisita(LocalDateTime.now()));
            activos++;
        }
    }
}
