package com.giza.center_reservation.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuntimeBusinessException extends RuntimeException{

    private String message;

    public RuntimeBusinessException(String message) {
        super(message);
        this.message = message;
    }
}
