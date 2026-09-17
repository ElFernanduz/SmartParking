package com.tecno.Smartparking.service;

import com.tecno.Smartparking.exception.NegocioException;
import com.tecno.Smartparking.model.EstadoOperativo;
import com.tecno.Smartparking.model.Parametro;
import com.tecno.Smartparking.repository.ParametroRepository;
import com.tecno.Smartparking.service.comando.FabricaComandos;
import com.tecno.Smartparking.service.comando.InvocadorComandos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capacidad y umbral de humo, ajustables sin recompilar. */
@Service
public class ConfiguracionService {

    private static final int NIVEL_MAXIMO_ADC = 4095;
    private static final int CAPACIDAD_POR_DEFECTO = 6;
    private static final int UMBRAL_POR_DEFECTO = 400;

    private final ParametroRepository parametros;
    private final EstadoParqueaderoService estado;
    private final DispositivoService dispositivo;
    private final FabricaComandos comandos;
    private final InvocadorComandos invocador;
    private final NotificacionService notificaciones;

    public ConfiguracionService(ParametroRepository parametros,
                                EstadoParqueaderoService estado,
                                DispositivoService dispositivo,
                                FabricaComandos comandos,
                                InvocadorComandos invocador,
                                NotificacionService notificaciones) {
        this.parametros = parametros;
        this.estado = estado;
        this.dispositivo = dispositivo;
        this.comandos = comandos;
        this.invocador = invocador;
        this.notificaciones = notificaciones;
    }

    @Transactional(readOnly = true)
    public int obtenerCapacidad() {
        return leerEntero(Parametro.CAPACIDAD_TOTAL, CAPACIDAD_POR_DEFECTO);
    }

    @Transactional(readOnly = true)
    public int obtenerUmbral() {
        return leerEntero(Parametro.UMBRAL_HUMO, UMBRAL_POR_DEFECTO);
    }

    @Transactional(readOnly = true)
    public EstadoOperativo obtenerEstadoSistema() {
        return parametros.findById(Parametro.ESTADO_SISTEMA)
                .map(parametro -> EstadoOperativo.valueOf(parametro.getValor()))
                .orElse(EstadoOperativo.OPERATIVO);
    }

    @Transactional
    public void actualizarCapacidad(int capacidad) {
        if (capacidad <= 0) {
            throw new NegocioException("La capacidad debe ser mayor que cero");
        }
        guardar(Parametro.CAPACIDAD_TOTAL, String.valueOf(capacidad));
        estado.actualizarCapacidad(capacidad);

        invocador.ejecutar(comandos.actualizarPantalla(estado.getCuposDisponibles()));
        notificaciones.cuposCambiados(estado.getCuposDisponibles(), estado.getCapacidadTotal());
    }

    @Transactional
    public void actualizarUmbral(int umbral) {
        if (umbral < 0 || umbral > NIVEL_MAXIMO_ADC) {
            throw new NegocioException("El umbral debe estar entre 0 y " + NIVEL_MAXIMO_ADC);
        }
        guardar(Parametro.UMBRAL_HUMO, String.valueOf(umbral));
        dispositivo.configurarUmbral(umbral);
    }

    @Transactional
    public void guardarEstadoSistema(EstadoOperativo nuevo) {
        guardar(Parametro.ESTADO_SISTEMA, nuevo.name());
    }

    private int leerEntero(String clave, int porDefecto) {
        return parametros.findById(clave)
                .map(parametro -> Integer.parseInt(parametro.getValor()))
                .orElse(porDefecto);
    }

    private void guardar(String clave, String valor) {
        Parametro parametro = parametros.findById(clave)
                .orElseGet(() -> new Parametro(clave, valor));
        parametro.setValor(valor);
        parametros.save(parametro);
    }
}
