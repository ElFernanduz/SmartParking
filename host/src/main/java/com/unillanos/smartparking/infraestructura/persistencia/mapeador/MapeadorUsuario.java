package com.unillanos.smartparking.infraestructura.persistencia.mapeador;

import com.unillanos.smartparking.dominio.autenticacion.Credential;
import com.unillanos.smartparking.dominio.autenticacion.Permission;
import com.unillanos.smartparking.dominio.autenticacion.Role;
import com.unillanos.smartparking.dominio.autenticacion.User;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.PermisoEntity;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.RolEntity;
import com.unillanos.smartparking.infraestructura.persistencia.entidad.UsuarioEntity;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MapeadorUsuario {

    private MapeadorUsuario() {
    }

    public static UsuarioEntity aEntidad(User user) {
        Set<RolEntity> roles = user.getRoles().stream()
                .map(MapeadorUsuario::aEntidad)
                .collect(Collectors.toSet());

        return new UsuarioEntity(user.getId().toString(), user.getUsername(), user.getEmail(),
                user.getCredential().getPasswordHash(), user.getCredential().getSalt(),
                user.isActive() ? 1 : 0,
                MapeadorFechas.aTexto(user.getCreatedAt()),
                MapeadorFechas.aTexto(user.getCredential().getLastChangedAt()),
                roles);
    }

    public static RolEntity aEntidad(Role rol) {
        Set<PermisoEntity> permisos = rol.getPermissions().stream()
                .map(permiso -> new PermisoEntity(permiso.getCode()))
                .collect(Collectors.toSet());
        return new RolEntity(rol.getName(), permisos);
    }

    public static User aDominio(UsuarioEntity entidad) {
        Credential credencial = new Credential(entidad.getPasswordHash(), entidad.getSalt(),
                MapeadorFechas.aFecha(entidad.getClaveCambiadaEn()));

        User user = new User(UUID.fromString(entidad.getId()), entidad.getUsername(),
                entidad.getEmail(), credencial, entidad.getActivo() != 0,
                MapeadorFechas.aFecha(entidad.getCreadoEn()));

        entidad.getRoles().forEach(rolEntidad -> user.addRole(aDominio(rolEntidad)));
        return user;
    }

    public static Role aDominio(RolEntity entidad) {
        Role rol = new Role(entidad.getNombre());
        entidad.getPermisos().forEach(permiso -> rol.addPermission(new Permission(permiso.getCodigo())));
        return rol;
    }
}
