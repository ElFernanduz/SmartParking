package com.unillanos.smartparking.dominio.puerto.salida;

import com.unillanos.smartparking.dominio.autenticacion.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsernameOrEmail(String value);

    void save(User user);

    List<User> findAll();
}
