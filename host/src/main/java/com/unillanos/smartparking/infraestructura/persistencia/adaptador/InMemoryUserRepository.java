package com.unillanos.smartparking.infraestructura.persistencia.adaptador;

import com.unillanos.smartparking.dominio.autenticacion.User;
import com.unillanos.smartparking.dominio.puerto.salida.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de usuarios en memoria. No es el que usa la aplicacion, que
 * persiste en SQLite, pero sirve para probar el modelo sin base de datos.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> porUsuario = new HashMap<>();
    private final Map<String, User> porCorreo = new HashMap<>();

    @Override
    public Optional<User> findByUsernameOrEmail(String value) {
        User encontrado = porUsuario.get(value);
        if (encontrado == null) {
            encontrado = porCorreo.get(value);
        }
        return Optional.ofNullable(encontrado);
    }

    @Override
    public void save(User user) {
        porUsuario.put(user.getUsername(), user);
        porCorreo.put(user.getEmail(), user);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(porUsuario.values());
    }
}
