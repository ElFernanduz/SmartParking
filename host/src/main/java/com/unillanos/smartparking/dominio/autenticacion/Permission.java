package com.unillanos.smartparking.dominio.autenticacion;

import java.util.Objects;

/** Permiso concreto sobre una accion del sistema, identificado por su codigo. */
public class Permission {

    private final String code;

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
            return code.equals(permiso.code);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code;
    }
}
