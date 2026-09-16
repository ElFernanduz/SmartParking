package com.unillanos.smartparking.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unillanos.smartparking.aplicacion.seguridad.AuthorizationService;
import com.unillanos.smartparking.dominio.autenticacion.Credential;
import com.unillanos.smartparking.dominio.autenticacion.Permission;
import com.unillanos.smartparking.dominio.autenticacion.Permisos;
import com.unillanos.smartparking.dominio.autenticacion.Role;
import com.unillanos.smartparking.dominio.autenticacion.User;
import com.unillanos.smartparking.infraestructura.persistencia.adaptador.UserRepositoryAdapter;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserRepositoryAdapter.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/smartparking-usuarios.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:db/schema.sql"
})
class UsuariosPersistenciaTest {

    private final AuthorizationService autorizacion = new AuthorizationService();

    @Autowired
    private UserRepositoryAdapter usuarios;

    private User nuevoOperador(String username, String email) {
        Role rol = new Role("OPERADOR");
        rol.addPermission(new Permission(Permisos.BARRERA_CONTROL));
        rol.addPermission(new Permission(Permisos.EMERGENCIA_CONTROL));

        User user = new User(username, email, new Credential("hash-de-prueba", "sal-de-prueba"));
        user.addRole(rol);
        return user;
    }

    @Test
    @DisplayName("Un usuario guardado se recupera por su nombre de usuario")
    void recuperarPorNombreDeUsuario() {
        usuarios.save(nuevoOperador("carlos", "carlos@unillanos.edu.co"));

        Optional<User> encontrado = usuarios.findByUsernameOrEmail("carlos");

        assertTrue(encontrado.isPresent());
        assertEquals("carlos@unillanos.edu.co", encontrado.get().getEmail());
        assertTrue(encontrado.get().isActive());
    }

    @Test
    @DisplayName("El mismo usuario se recupera por su correo")
    void recuperarPorCorreo() {
        usuarios.save(nuevoOperador("marta", "marta@unillanos.edu.co"));

        assertTrue(usuarios.findByUsernameOrEmail("marta@unillanos.edu.co").isPresent());
        assertTrue(usuarios.findByUsernameOrEmail("nadie").isEmpty());
    }

    @Test
    @DisplayName("Los roles y sus permisos sobreviven al guardado")
    void losRolesYPermisosSePersisten() {
        usuarios.save(nuevoOperador("sofia", "sofia@unillanos.edu.co"));

        User recuperado = usuarios.findByUsernameOrEmail("sofia").orElseThrow();

        assertEquals(1, recuperado.getRoles().size());
        assertTrue(autorizacion.hasPermission(recuperado, Permisos.BARRERA_CONTROL));
        assertTrue(autorizacion.hasPermission(recuperado, Permisos.EMERGENCIA_CONTROL));
        assertFalse(autorizacion.hasPermission(recuperado, Permisos.CONFIG_UPDATE));
    }

    @Test
    @DisplayName("La credencial se guarda cifrada y se recupera intacta")
    void laCredencialSeConserva() {
        usuarios.save(nuevoOperador("diego", "diego@unillanos.edu.co"));

        User recuperado = usuarios.findByUsernameOrEmail("diego").orElseThrow();

        assertEquals("hash-de-prueba", recuperado.getCredential().getPasswordHash());
        assertEquals("sal-de-prueba", recuperado.getCredential().getSalt());
    }

    @Test
    @DisplayName("Desactivar un usuario se refleja al volver a leerlo")
    void ladesactivacionSePersiste() {
        User user = nuevoOperador("ana", "ana@unillanos.edu.co");
        usuarios.save(user);

        user.deactivate();
        usuarios.save(user);

        assertFalse(usuarios.findByUsernameOrEmail("ana").orElseThrow().isActive());
    }
}
