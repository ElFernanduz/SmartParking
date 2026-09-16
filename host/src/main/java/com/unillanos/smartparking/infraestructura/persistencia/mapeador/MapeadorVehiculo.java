package com.unillanos.smartparking.infraestructura.persistencia.mapeador;

import com.unillanos.smartparking.dominio.modelo.TipoVehiculo;
import com.unillanos.smartparking.dominio.modelo.Vehiculo;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.VehiculoEntity;

public final class MapeadorVehiculo {

    private MapeadorVehiculo() {
    }

    public static VehiculoEntity aEntidad(Vehiculo vehiculo) {
        String tipo = vehiculo.getTipoVehiculo() == null ? null : vehiculo.getTipoVehiculo().name();
        return new VehiculoEntity(vehiculo.getPlaca(), tipo,
                MapeadorFechas.aTexto(vehiculo.getFechaRegistro()));
    }

    public static Vehiculo aDominio(VehiculoEntity entidad) {
        TipoVehiculo tipo = entidad.getTipoVehiculo() == null
                ? null
                : TipoVehiculo.valueOf(entidad.getTipoVehiculo());
        return new Vehiculo(entidad.getPlaca(), tipo, MapeadorFechas.aFecha(entidad.getFechaRegistro()));
    }
}
