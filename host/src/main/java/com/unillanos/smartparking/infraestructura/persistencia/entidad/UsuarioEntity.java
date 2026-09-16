package com.unillanos.smartparking.infraestructura.persistencia.entidad;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class UsuarioEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "salt", nullable = false)
    private String salt;

    /** Booleano guardado como entero, segun la convencion del esquema. */
    @Column(name = "activo", nullable = false)
    private int activo;

    @Column(name = "creado_en", nullable = false)
    private String creadoEn;

    @Column(name = "clave_cambiada_en", nullable = false)
    private String claveCambiadaEn;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_nombre"))
    private Set<RolEntity> roles = new HashSet<>();

    protected UsuarioEntity() {
    }

    public UsuarioEntity(String id, String username, String email, String passwordHash,
                         String salt, int activo, String creadoEn, String claveCambiadaEn,
                         Set<RolEntity> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.activo = activo;
        this.creadoEn = creadoEn;
        this.claveCambiadaEn = claveCambiadaEn;
        this.roles = roles;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public int getActivo() {
        return activo;
    }

    public String getCreadoEn() {
        return creadoEn;
    }

    public String getClaveCambiadaEn() {
        return claveCambiadaEn;
    }

    public Set<RolEntity> getRoles() {
        return roles;
    }
}
