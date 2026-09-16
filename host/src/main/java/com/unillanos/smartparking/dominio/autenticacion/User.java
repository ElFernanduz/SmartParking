package com.unillanos.smartparking.dominio.autenticacion;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Usuario del sistema. Posee su credencial por composicion y roles por asociacion. */
public class User {

    private final UUID id;
    private final String username;
    private final String email;
    private boolean active;
    private final LocalDateTime createdAt;
    private final Credential credential;
    private final Set<Role> roles = new HashSet<>();

    public User(UUID id, String username, String email, Credential credential,
                boolean active, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.username = Objects.requireNonNull(username);
        this.email = Objects.requireNonNull(email);
        this.credential = Objects.requireNonNull(credential);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public User(String username, String email, Credential credential) {
        this(UUID.randomUUID(), username, email, credential, true, LocalDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Credential getCredential() {
        return credential;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
