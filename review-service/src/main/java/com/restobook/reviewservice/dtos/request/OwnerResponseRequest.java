package com.restobook.reviewservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerResponseRequest {

    @NotBlank(message = "La réponse ne peut pas être vide")
    @Size(max = 1000, message = "La réponse ne peut pas dépasser 1000 caractères")
    private String response;
}
