package lu.esante.agence.epione.controller;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lu.esante.agence.epione.model.EpioneError;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(value = { lu.esante.agence.epione.exception.BadRequestException.class,
            lu.esante.agence.epione.exception.DocumentFormatException.class })
    public ResponseEntity<EpioneError> badRequestException(Exception ex, WebRequest request) {
        EpioneError err = EpioneError.builder()
                .message(ex.getMessage())
                .code("HTTP 400")
                .timestamp(OffsetDateTime.now())
                .build();

        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class })
    public ResponseEntity<EpioneError> argumentMismatchException(Exception ex, WebRequest request) {

        MethodArgumentTypeMismatchException error = (MethodArgumentTypeMismatchException) ex;

        EpioneError err = EpioneError.builder()
                .message("Error when parsing parameter " + error.getName())
                .code("HTTP - 400")
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { lu.esante.agence.epione.exception.ForbiddenException.class })
    public ResponseEntity<EpioneError> forbidden(Exception ex, WebRequest request) {
        EpioneError err = EpioneError.builder()
                .message(ex.getMessage())
                .code("HTTP 403")
                .timestamp(OffsetDateTime.now())
                .build();

        return new ResponseEntity<>(err, HttpStatus.FORBIDDEN);
    }

}
