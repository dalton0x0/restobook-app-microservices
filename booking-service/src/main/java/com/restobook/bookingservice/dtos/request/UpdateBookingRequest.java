package com.restobook.bookingservice.dtos.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingRequest {

    @FutureOrPresent(message = "La date de réservation ne peut pas être dans le passé")
    private LocalDate bookingDate;

    private LocalTime bookingTime;

    @Min(value = 1, message = "Le nombre de personnes doit être d'au moins 1")
    @Max(value = 8, message = "Le nombre de personnes ne peut pas dépasser 8")
    private Integer partySize;

    @Size(max = 500, message = "Les demandes spéciales ne peuvent pas dépasser 500 caractères")
    private String specialRequests;

    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String customerName;

    @Email(message = "Format d'email invalide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String customerEmail;

    @Pattern(regexp = "^(\\+33|0)[1-9](\\d{8})$", message = "Format de téléphone invalide")
    private String customerPhone;
}
