package com.restobook.restaurantservice.dtos.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRestaurantRequest {

    @NotBlank(message = "Le nom du restaurant est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 200, message = "L'adresse ne peut pas dépasser 200 caractères")
    private String address;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String city;

    @Size(max = 5, message = "Le code postal ne peut pas dépasser 5 caractères")
    private String postalCode;

    @Pattern(regexp = "^(\\+33|0)[1-9](\\d{8})$", message = "Format de téléphone invalide")
    private String phone;

    @Email(regexp = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$",
            message = "Format d'email invalide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    @Size(max = 500, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
    private String imageUrl;

    @Size(max = 50, message = "Le type de cuisine ne peut pas dépasser 50 caractères")
    private String cuisineType;

    @NotNull(message = "La capacité totale est obligatoire")
    @Min(value = 1, message = "La capacité doit être d'au moins 1 place")
    @Max(value = 500, message = "La capacité ne peut pas dépasser 500 places")
    private Integer totalCapacity;

    private List<OpeningHoursRequest> openingHours;
}
