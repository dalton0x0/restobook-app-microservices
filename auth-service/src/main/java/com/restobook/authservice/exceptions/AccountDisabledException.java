package com.restobook.authservice.exceptions;

import com.restobook.authservice.constants.ExceptionConst;
import org.springframework.http.HttpStatus;

public class AccountDisabledException extends BusinessException {

    public AccountDisabledException() {
        super("Ce compte est désactivé", HttpStatus.FORBIDDEN, ExceptionConst.ACCOUNT_DISABLED);
    }

    public AccountDisabledException(String message) {
        super(message, HttpStatus.FORBIDDEN, ExceptionConst.ACCOUNT_DISABLED);
    }
}
