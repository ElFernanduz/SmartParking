package com.tecno.Smartparking.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Usuario del sistema: posee su credencial y se asocia a roles. */
@Entity
@Table(name = "usuario")
public class User implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** Booleano guardado como entero, segun la convencion del esquema. */
    @Column(name = "activo", nullable = false)
    private int activo;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime createdAt;

    @Embedded
    private Credential credential;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_nombre"))
    private Set<Role> roles = new HashSet<>();

    protected User() {
    }

    public User(String username, String email, Credential credential) {
        this.id = UUID.randomUUID().toString();
        this.username = Objects.requireNonNull(username);
        this.email = Objects.requireNonNull(email);
        this.credential = Objects.requireNonNull(credential);
        this.activo = 1;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return activo != 0;
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
        this.activo = 1;
    }

    public void deactivate() {
        this.activo = 0;
    }
}
