package com.unillanos.smartparking.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Ambos campos son opcionales: se actualiza solo lo que venga. */
public record ConfiguracionRequest(@Min(1) @Max(1000) Integer capacidadTotal,
                                   @Min(0) @Max(4095) Integer umbralHumo) {
}
