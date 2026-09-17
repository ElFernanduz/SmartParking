package com.tecno.Smartparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Configuracion del sistema guardada como pares clave-valor. */
@Entity
@Table(name = "configuracion")
public class Parametro {

    public static final String CAPACIDAD_TOTAL = "capacidad_total";
    public static final String UMBRAL_HUMO = "umbral_humo";
    public static final String ESTADO_SISTEMA = "estado_sistema";

    @Id
    @Column(name = "clave")
    private String clave;

    @Column(name = "valor", nullable = false)
    private String valor;

    protected Parametro() {
    }

    public Parametro(String clave, String valor) {
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
