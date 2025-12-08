package com.restobook.authservice.exceptions;

import org.springframework.http.HttpStatus;

public class SecurityException extends BusinessException {

    public SecurityException() {
        super("Un problème de sécurité est survenu", HttpStatus.UNAUTHORIZED, "SECURITY_ERROR");
    }

    public SecurityException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "SECURITY_ERROR");
    }
}
