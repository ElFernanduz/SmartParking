package com.tecno.Smartparking.config;

import com.tecno.Smartparking.model.Credential;
import com.tecno.Smartparking.model.Permission;
import com.tecno.Smartparking.model.Role;
import com.tecno.Smartparking.model.User;
import com.tecno.Smartparking.repository.UserRepository;
import com.tecno.Smartparking.service.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void run(ApplicationArguments argumentos) {
        if (usuarios.findByUsernameOrEmail(usuarioAdmin).isPresent()) {
            return;
        }

        Role administrador = new Role("ADMIN");
        administrador.addPermission(new Permission(Permission.BARRERA_CONTROL));
        administrador.addPermission(new Permission(Permission.EMERGENCIA_CONTROL));
        administrador.addPermission(new Permission(Permission.CONFIG_UPDATE));
        administrador.addPermission(new Permission(Permission.CUPOS_SYNC));

        String sal = hasher.nuevaSal();
        User admin = new User(usuarioAdmin, correoAdmin,
                new Credential(hasher.hash(claveAdmin, sal), sal));
        admin.addRole(administrador);

        usuarios.save(admin);
        LOG.info("Usuario administrador creado: {}", usuarioAdmin);
    }
}
