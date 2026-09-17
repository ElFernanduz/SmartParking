package com.tecno.Smartparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/** Permiso sobre una accion del sistema, identificado por su codigo. */
@Entity
@Table(name = "permiso")
public class Permission {

    public static final String BARRERA_CONTROL = "BARRERA_CONTROL";
    public static final String EMERGENCIA_CONTROL = "EMERGENCIA_CONTROL";
    public static final String CONFIG_UPDATE = "CONFIG_UPDATE";
    public static final String CUPOS_SYNC = "CUPOS_SYNC";

    @Id
    @Column(name = "codigo")
    private String code;

    protected Permission() {
    }

    public Permission(String code) {
        this.code = Objects.requireNonNull(code);
    }

    public String getCode() {
        return code;
    }

    // El codigo es la identidad: dos permisos con el mismo codigo son el mismo.
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro instanceof Permission permiso) {
            return Objects.equals(code, permiso.code);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
