package com.restobook.restaurantservice.exceptions;

import com.restobook.restaurantservice.constants.ExceptionConst;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException{

    public ResourceNotFoundException(String ressource, String field, Object value) {
        super(
                String.format("%s non trouvé avec %s: '%s'", ressource, field, value),
                HttpStatus.NOT_FOUND,
                ExceptionConst.RESOURCE_NOT_FOUND
        );
    }

    public ResourceNotFoundException(String field, Object value) {
        super(
                String.format("Restaurant non trouvé avec %s: '%s'", field, value),
                HttpStatus.NOT_FOUND,
                ExceptionConst.RESOURCE_NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ExceptionConst.RESOURCE_NOT_FOUND);
    }
}
