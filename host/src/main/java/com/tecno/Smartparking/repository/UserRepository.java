package com.tecno.Smartparking.repository;

import com.tecno.Smartparking.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {

    @Query("select u from User u where u.username = :valor or u.email = :valor")
    Optional<User> findByUsernameOrEmail(@Param("valor") String valor);
}
