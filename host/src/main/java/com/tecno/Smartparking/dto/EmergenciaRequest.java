package com.tecno.Smartparking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EmergenciaRequest(@NotNull @Pattern(regexp = "ACTIVAR|LIMPIAR") String accion) {
}
