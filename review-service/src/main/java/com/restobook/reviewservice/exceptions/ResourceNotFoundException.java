package com.restobook.reviewservice.exceptions;

import com.restobook.reviewservice.constants.ExceptionConst;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(
                String.format("%s non trouvé avec %s: '%s'", resource, field, value),
                HttpStatus.NOT_FOUND,
                ExceptionConst.RESOURCE_NOT_FOUND
        );
    }

    public ResourceNotFoundException(String field, Object value) {
        super(
                String.format("Avis non trouvé avec %s: '%s'", field, value),
                HttpStatus.NOT_FOUND,
                ExceptionConst.RESOURCE_NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ExceptionConst.RESOURCE_NOT_FOUND);
    }
}
