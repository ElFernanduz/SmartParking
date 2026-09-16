package com.unillanos.smartparking.dominio.autenticacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModeloAutenticacionTest {

    private static User usuarioDePrueba() {
        return new User("operador", "operador@unillanos.edu.co", new Credential("hash", "sal"));
    }

    @Test
    @DisplayName("Un usuario nace activo y con identificador propio")
    void usuarioNaceActivo() {
        User user = usuarioDePrueba();

        assertTrue(user.isActive());
        assertTrue(user.getRoles().isEmpty());
        assertEquals("operador", user.getUsername());
        assertTrue(user.getId() != null);
    }

    @Test
    @DisplayName("Un usuario se puede desactivar y volver a activar")
    void desactivarYActivar() {
        User user = usuarioDePrueba();

        user.deactivate();
        assertFalse(user.isActive());

        user.activate();
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("Los permisos llegan al usuario a traves de sus roles")
    void losPermisosLleganPorLosRoles() {
        Role operador = new Role("OPERADOR");
        operador.addPermission(new Permission(Permisos.BARRERA_CONTROL));
        User user = usuarioDePrueba();

        user.addRole(operador);

        assertEquals(1, user.getRoles().size());
        assertTrue(operador.hasPermission(Permisos.BARRERA_CONTROL));
        assertFalse(operador.hasPermission(Permisos.CONFIG_UPDATE));
    }

    @Test
    @DisplayName("Dos permisos con el mismo codigo son el mismo permiso")
    void losPermisosSeIdentificanPorSuCodigo() {
        Set<Permission> permisos = Set.of(new Permission("CONFIG_UPDATE"));

        assertTrue(permisos.contains(new Permission("CONFIG_UPDATE")));
        assertEquals(new Permission("X"), new Permission("X"));
        assertFalse(new Permission("X").equals(new Permission("Y")));
    }

    @Test
    @DisplayName("Agregar dos veces el mismo rol no lo duplica")
    void losRolesNoSeDuplican() {
        User user = usuarioDePrueba();

        user.addRole(new Role("ADMIN"));
        user.addRole(new Role("ADMIN"));

        assertEquals(1, user.getRoles().size());
    }

    @Test
    @DisplayName("Las colecciones expuestas son de solo lectura")
    void lasColeccionesNoSeModificanDesdeFuera() {
        User user = usuarioDePrueba();
        Role rol = new Role("ADMIN");
        user.addRole(rol);

        assertThrows(UnsupportedOperationException.class,
                () -> user.getRoles().add(new Role("OTRO")));
        assertThrows(UnsupportedOperationException.class,
                () -> rol.getPermissions().add(new Permission("OTRO")));
    }

    @Test
    @DisplayName("Un usuario no puede construirse sin credencial")
    void laCredencialEsObligatoria() {
        assertThrows(NullPointerException.class,
                () -> new User("x", "x@x.co", null));
    }
}
