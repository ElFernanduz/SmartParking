package com.unillanos.smartparking.infraestructura.persistencia.adaptador;

import com.unillanos.smartparking.dominio.autenticacion.User;
import com.unillanos.smartparking.dominio.puerto.salida.UserRepository;
import com.unillanos.smartparking.infraestructura.persistencia.mapeador.MapeadorUsuario;
import com.unillanos.smartparking.infraestructura.persistencia.repositorio.UsuarioJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Usuarios persistidos en SQLite, para que sobrevivan a un reinicio. */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UsuarioJpaRepository repositorio;

    public UserRepositoryAdapter(UsuarioJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String value) {
        Optional<User> porUsuario = repositorio.findByUsername(value).map(MapeadorUsuario::aDominio);
        if (porUsuario.isPresent()) {
            return porUsuario;
        }
        return repositorio.findByEmail(value).map(MapeadorUsuario::aDominio);
    }

    @Override
    @Transactional
    public void save(User user) {
        repositorio.save(MapeadorUsuario.aEntidad(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repositorio.findAll().stream().map(MapeadorUsuario::aDominio).toList();
    }
}
