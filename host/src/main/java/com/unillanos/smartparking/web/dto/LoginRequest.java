package com.unillanos.smartparking.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String usuario, @NotBlank String contrasena) {
}
