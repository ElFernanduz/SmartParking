package com.tecno.Smartparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.Objects;

/** Credencial de un usuario. El usuario la posee por composicion. */
@Embeddable
public class Credential {

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "salt", nullable = false)
    private String salt;

    @Column(name = "clave_cambiada_en", nullable = false)
    private LocalDateTime lastChangedAt;

    protected Credential() {
    }

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
