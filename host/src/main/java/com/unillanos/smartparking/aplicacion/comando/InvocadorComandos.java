package com.unillanos.smartparking.aplicacion.comando;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Ejecuta los comandos y centraliza el registro de las acciones sobre el hardware. */
public class InvocadorComandos {

    private static final Logger LOG = LoggerFactory.getLogger(InvocadorComandos.class);

    public void ejecutar(Comando comando) {
        LOG.info("Comando: {}", comando.descripcion());
        comando.ejecutar();
    }
}
