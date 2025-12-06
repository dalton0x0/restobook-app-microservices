package com.restobook.authservice.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenValidationResponse {

    private Boolean valid;
    private Long userId;
    private String email;
    private String role;
    private String message;

    public static TokenValidationResponse valid(Long userId, String email, String role) {
        return TokenValidationResponse.builder()
                .valid(true)
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }

    public static TokenValidationResponse invalid(String message) {
        return TokenValidationResponse.builder()
                .valid(false)
                .message(message)
                .build();
    }
}
