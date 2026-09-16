package com.unillanos.smartparking.infraestructura.persistencia.adaptador;

import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;
import com.unillanos.smartparking.dominio.puerto.salida.RepositorioConfiguracion;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.ConfiguracionEntity;
import com.unillanos.smartparking.infraestructura.persistencia.repositorio.ConfiguracionJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** La configuracion vive como pares clave-valor en la tabla configuracion. */
@Component
public class RepositorioConfiguracionAdapter implements RepositorioConfiguracion {

    private static final String CLAVE_CAPACIDAD = "capacidad_total";
    private static final String CLAVE_UMBRAL = "umbral_humo";
    private static final String CLAVE_ESTADO = "estado_sistema";

    private static final int CAPACIDAD_POR_DEFECTO = 6;
    private static final int UMBRAL_POR_DEFECTO = 400;

    private final ConfiguracionJpaRepository repositorio;

    public RepositorioConfiguracionAdapter(ConfiguracionJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public int obtenerCapacidad() {
        return leerEntero(CLAVE_CAPACIDAD, CAPACIDAD_POR_DEFECTO);
    }

    @Override
    @Transactional(readOnly = true)
    public int obtenerUmbral() {
        return leerEntero(CLAVE_UMBRAL, UMBRAL_POR_DEFECTO);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoOperativo obtenerEstadoSistema() {
        return repositorio.findById(CLAVE_ESTADO)
                .map(entidad -> EstadoOperativo.valueOf(entidad.getValor()))
                .orElse(EstadoOperativo.OPERATIVO);
    }

    @Override
    @Transactional
    public void guardarCapacidad(int capacidad) {
        guardar(CLAVE_CAPACIDAD, String.valueOf(capacidad));
    }

    @Override
    @Transactional
    public void guardarUmbral(int umbral) {
        guardar(CLAVE_UMBRAL, String.valueOf(umbral));
    }

    @Override
    @Transactional
    public void guardarEstadoSistema(EstadoOperativo estado) {
        guardar(CLAVE_ESTADO, estado.name());
    }

    private int leerEntero(String clave, int porDefecto) {
        return repositorio.findById(clave)
                .map(entidad -> Integer.parseInt(entidad.getValor()))
                .orElse(porDefecto);
    }

    private void guardar(String clave, String valor) {
        ConfiguracionEntity entidad = repositorio.findById(clave)
                .orElseGet(() -> new ConfiguracionEntity(clave, valor));
        entidad.setValor(valor);
        repositorio.save(entidad);
    }
}
