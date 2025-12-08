package com.restobook.authservice.exceptions;

import com.restobook.authservice.constants.ExceptionConst;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s non trouvé avec %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND,
                ExceptionConst.RESOURCE_NOT_FOUND
        );
    }

    public ResourceNotFoundException(String fieldName, Object fieldValue) {
        super(
                String.format("Utilisateur non trouvé avec %s: '%s'", fieldName, fieldValue),
                HttpStatus.NOT_FOUND,
                ExceptionConst.RESOURCE_NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ExceptionConst.RESOURCE_NOT_FOUND);
    }
}
