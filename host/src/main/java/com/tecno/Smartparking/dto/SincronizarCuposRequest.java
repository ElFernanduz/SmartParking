package com.tecno.Smartparking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SincronizarCuposRequest(@NotNull @Min(0) Integer cuposOcupados) {
}
