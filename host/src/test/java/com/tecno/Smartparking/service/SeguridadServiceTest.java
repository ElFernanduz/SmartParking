package com.tecno.Smartparking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.tecno.Smartparking.model.Credential;
import com.tecno.Smartparking.model.Permission;
import com.tecno.Smartparking.model.Role;
import com.tecno.Smartparking.model.User;
import com.tecno.Smartparking.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeguridadServiceTest {

    private final PasswordHasher hasher = new SimplePasswordHasher();
    private final AuthorizationService autorizacion = new AuthorizationService();

    @Mock
    private UserRepository usuarios;

    private AuthenticationService autenticacion;
    private User operador;

    @BeforeEach
    void prepararUsuarios() {
        autenticacion = new AuthenticationService(usuarios, hasher);

        String sal = hasher.nuevaSal();
        operador = new User("operador", "operador@unillanos.edu.co",
                new Credential(hasher.hash("clave-segura", sal), sal));

        Role rol = new Role("OPERADOR");
        rol.addPermission(new Permission(Permission.BARRERA_CONTROL));
        rol.addPermission(new Permission(Permission.EMERGENCIA_CONTROL));
        operador.addRole(rol);

        when(usuarios.findByUsernameOrEmail("operador")).thenReturn(Optional.of(operador));
        when(usuarios.findByUsernameOrEmail("operador@unillanos.edu.co")).thenReturn(Optional.of(operador));
        when(usuarios.findByUsernameOrEmail("fantasma")).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Se inicia sesion con el nombre de usuario")
    void loginConNombreDeUsuario() {
        Optional<User> sesion = autenticacion.login("operador", "clave-segura");

        assertTrue(sesion.isPresent());
        assertEquals("operador", sesion.get().getUsername());
    }

    @Test
    @DisplayName("Se inicia sesion con el correo")
    void loginConCorreo() {
        assertTrue(autenticacion.login("operador@unillanos.edu.co", "clave-segura").isPresent());
    }

    @Test
    @DisplayName("Una contrasena incorrecta no concede acceso")
    void contrasenaIncorrecta() {
        assertTrue(autenticacion.login("operador", "otra-cosa").isEmpty());
    }

    @Test
    @DisplayName("Un usuario inexistente no concede acceso")
    void usuarioInexistente() {
        assertTrue(autenticacion.login("fantasma", "clave-segura").isEmpty());
    }

    @Test
    @DisplayName("Un usuario desactivado no puede entrar aunque la clave sea correcta")
    void usuarioDesactivado() {
        operador.deactivate();

        assertTrue(autenticacion.login("operador", "clave-segura").isEmpty());
    }

    @Test
    @DisplayName("El permiso se concede si alguno de sus roles lo incluye")
    void permisoConcedido() {
        assertTrue(autorizacion.hasPermission(operador, Permission.BARRERA_CONTROL));
        assertTrue(autorizacion.hasPermission(operador, Permission.EMERGENCIA_CONTROL));
    }

    @Test
    @DisplayName("El permiso se niega si ningun rol lo incluye")
    void permisoNegado() {
        assertFalse(autorizacion.hasPermission(operador, Permission.CONFIG_UPDATE));
    }

    @Test
    @DisplayName("Un usuario desactivado pierde todos sus permisos")
    void elUsuarioInactivoNoTienePermisos() {
        operador.deactivate();

        assertFalse(autorizacion.hasPermission(operador, Permission.BARRERA_CONTROL));
        assertTrue(autorizacion.permisosDe(operador).isEmpty());
    }

    @Test
    @DisplayName("Sin sesion no hay permisos")
    void sinUsuarioNoHayPermisos() {
        assertFalse(autorizacion.hasPermission(null, Permission.BARRERA_CONTROL));
    }

    @Test
    @DisplayName("Se listan los permisos y roles efectivos del usuario")
    void permisosYRolesEfectivos() {
        assertEquals(2, autorizacion.permisosDe(operador).size());
        assertEquals(Set.of("OPERADOR"), autorizacion.rolesDe(operador));
    }

    @Test
    @DisplayName("La misma clave con distinta sal produce hashes distintos")
    void laSalCambiaElHash() {
        assertNotEquals(hasher.hash("clave", hasher.nuevaSal()), hasher.hash("clave", hasher.nuevaSal()));
    }

    @Test
    @DisplayName("El hasher verifica la clave correcta y rechaza la incorrecta")
    void verificacionDelHasher() {
        String sal = hasher.nuevaSal();
        String hash = hasher.hash("clave", sal);

        assertTrue(hasher.verify("clave", sal, hash));
        assertFalse(hasher.verify("clave-mala", sal, hash));
    }

    @Test
    @DisplayName("La contrasena nunca se guarda en claro")
    void laContrasenaNoSeGuardaEnClaro() {
        assertNotEquals("clave-segura", operador.getCredential().getPasswordHash());
        assertFalse(operador.getCredential().getPasswordHash().contains("clave-segura"));
    }

    @Test
    @DisplayName("Dos permisos con el mismo codigo son el mismo permiso")
    void losPermisosSeIdentificanPorSuCodigo() {
        assertEquals(new Permission("X"), new Permission("X"));
        assertNotEquals(new Permission("X"), new Permission("Y"));
        assertTrue(Set.of(new Permission("CONFIG_UPDATE")).contains(new Permission("CONFIG_UPDATE")));
    }
}
