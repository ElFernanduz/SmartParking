package com.tecno.Smartparking.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Rol que agrupa permisos. El usuario los obtiene a traves de sus roles. */
@Entity
@Table(name = "rol")
public class Role {

    @Id
    @Column(name = "nombre")
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "rol_permiso",
            joinColumns = @JoinColumn(name = "rol_nombre"),
            inverseJoinColumns = @JoinColumn(name = "permiso_codigo"))
    private Set<Permission> permissions = new HashSet<>();

    protected Role() {
    }

    public Role(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void addPermission(Permission p) {
        permissions.add(p);
    }

    public boolean hasPermission(String code) {
        return permissions.stream().anyMatch(permiso -> permiso.getCode().equals(code));
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro instanceof Role rol) {
            return Objects.equals(name, rol.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
