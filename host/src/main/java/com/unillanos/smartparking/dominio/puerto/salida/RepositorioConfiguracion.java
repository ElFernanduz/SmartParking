package com.unillanos.smartparking.dominio.puerto.salida;

import com.unillanos.smartparking.dominio.modelo.EstadoOperativo;

public interface RepositorioConfiguracion {

    int obtenerCapacidad();

    int obtenerUmbral();

    EstadoOperativo obtenerEstadoSistema();

    void guardarCapacidad(int capacidad);

    void guardarUmbral(int umbral);

    void guardarEstadoSistema(EstadoOperativo estado);
}
