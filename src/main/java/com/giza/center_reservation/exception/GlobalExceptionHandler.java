package com.giza.center_reservation.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleException(Exception ex, WebRequest request){
        return ResponseEntity.internalServerError().body(ex.getMessage());

    }
}
