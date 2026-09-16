package com.unillanos.smartparking.infraestructura.config;

import com.unillanos.smartparking.dominio.autenticacion.Credential;
import com.unillanos.smartparking.dominio.autenticacion.PasswordHasher;
import com.unillanos.smartparking.dominio.autenticacion.Permission;
import com.unillanos.smartparking.dominio.autenticacion.Permisos;
import com.unillanos.smartparking.dominio.autenticacion.Role;
import com.unillanos.smartparking.dominio.autenticacion.User;
import com.unillanos.smartparking.dominio.puerto.salida.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial la primera vez que arranca el sistema. Sin
 * el no habria forma de entrar al tablero para operar.
 */
@Component
@Order(1)
public class InicializadorSeguridad implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorSeguridad.class);

    private final UserRepository usuarios;
    private final PasswordHasher hasher;
    private final String usuarioAdmin;
    private final String correoAdmin;
    private final String claveAdmin;

    public InicializadorSeguridad(UserRepository usuarios,
                                  PasswordHasher hasher,
                                  @Value("${smartparking.seguridad.admin.usuario}") String usuarioAdmin,
                                  @Value("${smartparking.seguridad.admin.email}") String correoAdmin,
                                  @Value("${smartparking.seguridad.admin.contrasena}") String claveAdmin) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.usuarioAdmin = usuarioAdmin;
        this.correoAdmin = correoAdmin;
        this.claveAdmin = claveAdmin;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (usuarios.findByUsernameOrEmail(usuarioAdmin).isPresent()) {
            return;
        }

        Role administrador = new Role("ADMIN");
        administrador.addPermission(new Permission(Permisos.BARRERA_CONTROL));
        administrador.addPermission(new Permission(Permisos.EMERGENCIA_CONTROL));
        administrador.addPermission(new Permission(Permisos.CONFIG_UPDATE));
        administrador.addPermission(new Permission(Permisos.CUPOS_SYNC));

        String sal = hasher.nuevaSal();
        User admin = new User(usuarioAdmin, correoAdmin,
                new Credential(hasher.hash(claveAdmin, sal), sal));
        admin.addRole(administrador);

        usuarios.save(admin);
        LOG.info("Usuario administrador creado: {}", usuarioAdmin);
    }
}
