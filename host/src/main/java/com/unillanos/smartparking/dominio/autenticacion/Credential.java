package com.unillanos.smartparking.dominio.autenticacion;

import java.time.LocalDateTime;
import java.util.Objects;

/** Credencial de un usuario. El usuario la posee por composicion. */
public class Credential {

    private final String passwordHash;
    private final String salt;
    private final LocalDateTime lastChangedAt;

    public Credential(String passwordHash, String salt, LocalDateTime lastChangedAt) {
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.salt = Objects.requireNonNull(salt);
        this.lastChangedAt = Objects.requireNonNull(lastChangedAt);
    }

    public Credential(String passwordHash, String salt) {
        this(passwordHash, salt, LocalDateTime.now());
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public LocalDateTime getLastChangedAt() {
        return lastChangedAt;
    }
}
