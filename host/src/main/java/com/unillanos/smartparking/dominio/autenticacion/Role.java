package com.unillanos.smartparking.dominio.autenticacion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Rol que agrupa permisos. Un usuario obtiene sus permisos a traves de sus roles. */
public class Role {

    private final String name;
    private final Set<Permission> permissions = new HashSet<>();

    public Role(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void addPermission(Permission p) {
        permissions.add(p);
    }

    public boolean hasPermission(String code) {
        return permissions.stream().anyMatch(permiso -> permiso.getCode().equals(code));
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro instanceof Role rol) {
            return name.equals(rol.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
