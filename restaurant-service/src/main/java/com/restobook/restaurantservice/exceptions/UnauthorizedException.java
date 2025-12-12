package com.restobook.restaurantservice.exceptions;

import com.restobook.restaurantservice.constants.ExceptionConst;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException{

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ExceptionConst.UNAUTHORIZED);
    }

    public UnauthorizedException() {
        this("Vous n'êtes pas autorisé à effectuer cette action");
    }
}
