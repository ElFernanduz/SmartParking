package com.unillanos.smartparking.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracion")
public class ConfiguracionEntity {

    @Id
    @Column(name = "clave")
    private String clave;

    @Column(name = "valor", nullable = false)
    private String valor;

    protected ConfiguracionEntity() {
    }

    public ConfiguracionEntity(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public String getClave() {
        return clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
