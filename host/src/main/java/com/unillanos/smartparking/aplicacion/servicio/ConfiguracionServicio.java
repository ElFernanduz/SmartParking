package com.unillanos.smartparking.aplicacion.servicio;

import com.unillanos.smartparking.aplicacion.comando.FabricaComandos;
import com.unillanos.smartparking.aplicacion.comando.InvocadorComandos;
import com.unillanos.smartparking.dominio.evento.CuposCambiadosEvento;
import com.unillanos.smartparking.dominio.excepcion.ValorInvalidoExcepcion;
import com.unillanos.smartparking.dominio.modelo.Configuracion;
import com.unillanos.smartparking.dominio.modelo.EstadoParqueadero;
import com.unillanos.smartparking.dominio.puerto.entrada.GestionarConfiguracionCasoUso;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPasarelaDispositivo;
import com.unillanos.smartparking.dominio.puerto.salida.PuertoPublicadorEventos;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioConfiguracion;

/** Capacidad y umbral de humo, ajustables sin recompilar. */
public class ConfiguracionServicio implements GestionarConfiguracionCasoUso {

    private static final int NIVEL_MAXIMO_ADC = 4095;

    private final EstadoParqueadero estadoParqueadero;
    private final RepositorioConfiguracion repositorioConfiguracion;
    private final PuertoPasarelaDispositivo pasarela;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final PuertoPublicadorEventos publicador;

    public ConfiguracionServicio(EstadoParqueadero estadoParqueadero,
                                 RepositorioConfiguracion repositorioConfiguracion,
                                 PuertoPasarelaDispositivo pasarela,
                                 FabricaComandos comandos,
                                 InvocadorComandos invocador,
                                 PuertoPublicadorEventos publicador) {
        this.estadoParqueadero = estadoParqueadero;
        this.repositorioConfiguracion = repositorioConfiguracion;
        this.pasarela = pasarela;
        this.comandos = comandos;
        this.invocador = invocador;
        this.publicador = publicador;
    }

    @Override
    public void actualizarCapacidad(int capacidad) {
        if (capacidad <= 0) {
            throw new ValorInvalidoExcepcion("La capacidad debe ser mayor que cero");
        }
        repositorioConfiguracion.guardarCapacidad(capacidad);
        estadoParqueadero.actualizarCapacidad(capacidad);

        invocador.ejecutar(comandos.actualizarPantalla(estadoParqueadero.getCuposDisponibles()));
        publicador.publicar(CuposCambiadosEvento.ahora(
                estadoParqueadero.getCuposDisponibles(), estadoParqueadero.getCapacidadTotal()));
    }

    @Override
    public void actualizarUmbral(int umbral) {
        if (umbral < 0 || umbral > NIVEL_MAXIMO_ADC) {
            throw new ValorInvalidoExcepcion("El umbral debe estar entre 0 y " + NIVEL_MAXIMO_ADC);
        }
        repositorioConfiguracion.guardarUmbral(umbral);
        pasarela.configurarUmbral(umbral);
    }

    @Override
    public Configuracion obtenerConfiguracion() {
        return new Configuracion(repositorioConfiguracion.obtenerCapacidad(),
                repositorioConfiguracion.obtenerUmbral(),
                estadoParqueadero.getEstado());
    }
}
