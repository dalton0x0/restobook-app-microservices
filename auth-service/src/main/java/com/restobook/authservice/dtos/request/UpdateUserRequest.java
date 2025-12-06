package com.restobook.authservice.dtos.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;

    @Size(max = 20, message = "Le téléphone ne doit pas dépasser 20 caractères")
    @Pattern(regexp = "^(\\+33|0)[1-9](\\d{2}){4}$", message = "Format de téléphone invalide")
    private String phone;
}
