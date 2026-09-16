package com.unillanos.smartparking.aplicacion.seguridad;

import com.unillanos.smartparking.dominio.autenticacion.PasswordHasher;
import com.unillanos.smartparking.dominio.autenticacion.User;
import com.unillanos.smartparking.dominio.puerto.salida.UserRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Autenticacion por nombre de usuario o correo mas contrasena. */
public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository users;
    private final PasswordHasher hasher;

    public AuthenticationService(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public Optional<User> login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) {
            return Optional.empty();
        }

        Optional<User> encontrado = users.findByUsernameOrEmail(usernameOrEmail.trim());
        if (encontrado.isEmpty()) {
            LOG.info("Intento de acceso con un usuario inexistente");
            return Optional.empty();
        }

        User user = encontrado.get();
        if (!user.isActive()) {
            LOG.info("Intento de acceso de un usuario inactivo: {}", user.getUsername());
            return Optional.empty();
        }

        boolean valida = hasher.verify(password, user.getCredential().getSalt(),
                user.getCredential().getPasswordHash());
        if (!valida) {
            LOG.info("Contrasena incorrecta para {}", user.getUsername());
            return Optional.empty();
        }

        LOG.info("Acceso concedido a {}", user.getUsername());
        return Optional.of(user);
    }
}
