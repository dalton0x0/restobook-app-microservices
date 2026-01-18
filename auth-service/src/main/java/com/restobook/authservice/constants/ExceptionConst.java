package com.restobook.authservice.constants;

public class ExceptionConst {

    private ExceptionConst(){
        throw new IllegalStateException("Utility class");
    }

    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String SECURITY_ERROR = "SECURITY_ERROR";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
