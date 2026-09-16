package com.unillanos.smartparking.aplicacion.seguridad;

import com.unillanos.smartparking.dominio.autenticacion.Permission;
import com.unillanos.smartparking.dominio.autenticacion.Role;
import com.unillanos.smartparking.dominio.autenticacion.User;
import java.util.Set;
import java.util.stream.Collectors;

/** Autorizacion basada en roles: los permisos llegan al usuario por sus roles. */
public class AuthorizationService {

    public boolean hasPermission(User user, String permissionCode) {
        if (user == null || !user.isActive()) {
            return false;
        }
        return user.getRoles().stream()
                .flatMap(rol -> rol.getPermissions().stream())
                .anyMatch(permiso -> permiso.getCode().equals(permissionCode));
    }

    public Set<String> permisosDe(User user) {
        if (user == null || !user.isActive()) {
            return Set.of();
        }
        return user.getRoles().stream()
                .flatMap(rol -> rol.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    public Set<String> rolesDe(User user) {
        if (user == null) {
            return Set.of();
        }
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }
}
