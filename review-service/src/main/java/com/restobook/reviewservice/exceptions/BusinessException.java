package com.restobook.reviewservice.exceptions;

import com.restobook.reviewservice.constants.ExceptionConst;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public BusinessException(String message, HttpStatus status) {
        this(message, status, ExceptionConst.BUSINESS_ERROR);
    }
}
