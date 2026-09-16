package com.unillanos.smartparking.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permiso")
public class PermisoEntity {

    @Id
    @Column(name = "codigo")
    private String codigo;

    protected PermisoEntity() {
    }

    public PermisoEntity(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
