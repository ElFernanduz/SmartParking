package com.unillanos.smartparking.aplicacion;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Procesa en serie todo lo que cambia el estado o escribe en la base. Con un
 * solo hilo no hay mutacion concurrente del conteo ni escrituras simultaneas
 * a SQLite.
 */
public class EjecutorSerie {

    private static final Logger LOG = LoggerFactory.getLogger(EjecutorSerie.class);

    private final ScheduledExecutorService ejecutor;

    public EjecutorSerie() {
        this.ejecutor = Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "smartparking-eventos");
            hilo.setDaemon(true);
            return hilo;
        });
    }

    public void ejecutar(Runnable tarea) {
        ejecutor.execute(() -> protegido(tarea));
    }

    public ScheduledFuture<?> programar(Runnable tarea, long milisegundos) {
        return ejecutor.schedule(() -> protegido(tarea), milisegundos, TimeUnit.MILLISECONDS);
    }

    public void detener() {
        ejecutor.shutdownNow();
    }

    /** Un fallo en una tarea no puede tumbar el hilo que procesa los eventos. */
    private void protegido(Runnable tarea) {
        try {
            tarea.run();
        } catch (RuntimeException e) {
            LOG.error("Fallo procesando un evento: {}", e.getMessage(), e);
        }
    }
}
