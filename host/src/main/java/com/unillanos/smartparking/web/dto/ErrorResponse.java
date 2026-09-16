package com.unillanos.smartparking.web.dto;

import java.time.LocalDateTime;

public record ErrorResponse(int estado, String mensaje, LocalDateTime timestamp) {

    public static ErrorResponse de(int estado, String mensaje) {
        return new ErrorResponse(estado, mensaje, LocalDateTime.now());
    }
}
