package com.restobook.bookingservice.constants;

public class ExceptionConst {

    private ExceptionConst(){
        throw new IllegalStateException("Utility class");
    }

    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
}
