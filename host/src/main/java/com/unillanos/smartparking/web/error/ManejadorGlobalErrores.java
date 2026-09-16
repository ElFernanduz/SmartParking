package com.unillanos.smartparking.web.error;

import com.unillanos.smartparking.dominio.excepcion.ExcepcionDominio;
import com.unillanos.smartparking.dominio.excepcion.SinCuposDisponiblesExcepcion;
import com.unillanos.smartparking.web.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones de dominio a codigos HTTP con un cuerpo claro. */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(SinCuposDisponiblesExcepcion.class)
    public ResponseEntity<ErrorResponse> sinCupos(SinCuposDisponiblesExcepcion e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ExcepcionDominio.class)
    public ResponseEntity<ErrorResponse> excepcionDeDominio(ExcepcionDominio e) {
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInesperado(Exception e) {
        LOG.error("Error inesperado atendiendo la peticion", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(ErrorResponse.de(estado.value(), mensaje));
    }
}
