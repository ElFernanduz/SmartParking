package com.tecno.Smartparking.service;

import com.tecno.Smartparking.model.Permission;
import com.tecno.Smartparking.model.Role;
import com.tecno.Smartparking.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Autorizacion basada en roles: los permisos llegan al usuario por sus roles. */
@Service
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
