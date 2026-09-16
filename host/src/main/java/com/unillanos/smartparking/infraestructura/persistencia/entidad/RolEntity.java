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
@Table(name = "rol")
public class RolEntity {

    @Id
    @Column(name = "nombre")
    private String nombre;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "rol_permiso",
            joinColumns = @JoinColumn(name = "rol_nombre"),
            inverseJoinColumns = @JoinColumn(name = "permiso_codigo"))
    private Set<PermisoEntity> permisos = new HashSet<>();

    protected RolEntity() {
    }

    public RolEntity(String nombre, Set<PermisoEntity> permisos) {
        this.nombre = nombre;
        this.permisos = permisos;
    }

    public String getNombre() {
        return nombre;
    }

    public Set<PermisoEntity> getPermisos() {
        return permisos;
    }
}
