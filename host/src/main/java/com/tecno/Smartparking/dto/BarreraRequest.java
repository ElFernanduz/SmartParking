package com.tecno.Smartparking.dto;

import com.tecno.Smartparking.model.TipoPunto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BarreraRequest(@NotNull TipoPunto punto,
                             @NotNull @Pattern(regexp = "ABRIR|CERRAR") String accion) {
}
