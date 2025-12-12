package com.restobook.restaurantservice.exceptions;

import com.restobook.restaurantservice.constants.ExceptionConst;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException{

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, ExceptionConst.FORBIDDEN);
    }

    public ForbiddenException() {
        this("Accès refusé ! Vous n'avez pas les droits suffisants pour effectuer cette action");
    }
}
