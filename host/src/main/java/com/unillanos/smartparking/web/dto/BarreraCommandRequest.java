package com.unillanos.smartparking.web.dto;

import com.unillanos.smartparking.dominio.modelo.TipoPunto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BarreraCommandRequest(@NotNull TipoPunto punto,
                                    @NotNull @Pattern(regexp = "ABRIR|CERRAR") String accion) {
}
