package com.smartparking.dto;

public class ActualizarEspaciosRequest {
    private int cantidad; // -1 = entra un vehículo, +1 = sale un vehículo

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
