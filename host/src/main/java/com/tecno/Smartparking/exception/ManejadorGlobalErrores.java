package com.tecno.Smartparking.exception;

import com.tecno.Smartparking.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Traduce las excepciones a codigos HTTP con un cuerpo de error claro. */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorResponse> errorDeNegocio(NegocioException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> peticionInvalida(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Peticion invalida");
        return respuesta(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> recursoNoEncontrado(NoResourceFoundException e) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado: " + e.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInesperado(Exception e) {
        LOG.error("Error inesperado atendiendo la peticion", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(ErrorResponse.de(estado.value(), mensaje));
    }
}
