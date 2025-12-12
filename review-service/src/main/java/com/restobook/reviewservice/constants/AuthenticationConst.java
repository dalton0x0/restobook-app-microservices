package com.restobook.reviewservice.constants;

public class AuthenticationConst {

    private AuthenticationConst(){
        throw new IllegalStateException("Utility class");
    }

    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
}
