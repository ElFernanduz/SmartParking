package com.unillanos.smartparking.dominio.puerto.entrada;

import com.unillanos.smartparking.dominio.modelo.Configuracion;

public interface GestionarConfiguracionCasoUso {

    void actualizarCapacidad(int capacidad);

    void actualizarUmbral(int umbral);

    Configuracion obtenerConfiguracion();
}
