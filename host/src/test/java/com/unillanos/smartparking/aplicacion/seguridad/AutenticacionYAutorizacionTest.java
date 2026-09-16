package com.unillanos.smartparking.aplicacion.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unillanos.smartparking.dominio.autenticacion.Credential;
import com.unillanos.smartparking.dominio.autenticacion.PasswordHasher;
import com.unillanos.smartparking.dominio.autenticacion.Permission;
import com.unillanos.smartparking.dominio.autenticacion.Permisos;
import com.unillanos.smartparking.dominio.autenticacion.Role;
import com.unillanos.smartparking.dominio.autenticacion.User;
import com.unillanos.smartparking.dominio.puerto.salida.UserRepository;
import com.unillanos.smartparking.infraestructura.persistencia.adaptador.InMemoryUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AutenticacionYAutorizacionTest {

    private final PasswordHasher hasher = new SimplePasswordHasher();
    private final AuthorizationService autorizacion = new AuthorizationService();

    private UserRepository usuarios;
    private AuthenticationService autenticacion;
    private User operador;

    @BeforeEach
    void prepararUsuarios() {
        usuarios = new InMemoryUserRepository();
        autenticacion = new AuthenticationService(usuarios, hasher);

        String sal = hasher.nuevaSal();
        operador = new User("operador", "operador@unillanos.edu.co",
                new Credential(hasher.hash("clave-seguro", sal), sal));

        Role rol = new Role("OPERADOR");
        rol.addPermission(new Permission(Permisos.BARRERA_CONTROL));
        rol.addPermission(new Permission(Permisos.EMERGENCIA_CONTROL));
        operador.addRole(rol);

        usuarios.save(operador);
    }

    @Test
    @DisplayName("Se inicia sesion con el nombre de usuario")
    void loginConNombreDeUsuario() {
        Optional<User> sesion = autenticacion.login("operador", "clave-seguro");

        assertTrue(sesion.isPresent());
        assertEquals("operador", sesion.get().getUsername());
    }

    @Test
    @DisplayName("Se inicia sesion con el correo")
    void loginConCorreo() {
        assertTrue(autenticacion.login("operador@unillanos.edu.co", "clave-seguro").isPresent());
    }

    @Test
    @DisplayName("Una contrasena incorrecta no concede acceso")
    void contrasenaIncorrecta() {
        assertTrue(autenticacion.login("operador", "otra-cosa").isEmpty());
    }

    @Test
    @DisplayName("Un usuario inexistente no concede acceso")
    void usuarioInexistente() {
        assertTrue(autenticacion.login("fantasma", "clave-seguro").isEmpty());
    }

    @Test
    @DisplayName("Un usuario desactivado no puede entrar aunque la clave sea correcta")
    void usuarioDesactivado() {
        operador.deactivate();

        assertTrue(autenticacion.login("operador", "clave-seguro").isEmpty());
    }

    @Test
    @DisplayName("El permiso se concede si alguno de sus roles lo incluye")
    void permisoConcedido() {
        assertTrue(autorizacion.hasPermission(operador, Permisos.BARRERA_CONTROL));
        assertTrue(autorizacion.hasPermission(operador, Permisos.EMERGENCIA_CONTROL));
    }

    @Test
    @DisplayName("El permiso se niega si ningun rol lo incluye")
    void permisoNegado() {
        assertFalse(autorizacion.hasPermission(operador, Permisos.CONFIG_UPDATE));
    }

    @Test
    @DisplayName("Un usuario desactivado pierde todos sus permisos")
    void elUsuarioInactivoNoTienePermisos() {
        operador.deactivate();

        assertFalse(autorizacion.hasPermission(operador, Permisos.BARRERA_CONTROL));
        assertTrue(autorizacion.permisosDe(operador).isEmpty());
    }

    @Test
    @DisplayName("Sin sesion no hay permisos")
    void sinUsuarioNoHayPermisos() {
        assertFalse(autorizacion.hasPermission(null, Permisos.BARRERA_CONTROL));
    }

    @Test
    @DisplayName("Se listan los permisos y roles efectivos del usuario")
    void permisosYRolesEfectivos() {
        assertEquals(2, autorizacion.permisosDe(operador).size());
        assertEquals(java.util.Set.of("OPERADOR"), autorizacion.rolesDe(operador));
    }

    @Test
    @DisplayName("La misma clave con distinta sal produce hashes distintos")
    void laSalCambiaElHash() {
        String hashUno = hasher.hash("clave", hasher.nuevaSal());
        String hashDos = hasher.hash("clave", hasher.nuevaSal());

        assertNotEquals(hashUno, hashDos);
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
        assertNotEquals("clave-seguro", operador.getCredential().getPasswordHash());
        assertFalse(operador.getCredential().getPasswordHash().contains("clave-seguro"));
    }
}
